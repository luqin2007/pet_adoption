package com.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.util.*;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.backend.util.C.KEY_PASSWORD_RESET;
import static com.example.backend.util.C.MESSAGE_RESET_PWD;

@Service
@RequiredArgsConstructor
public class UserService extends BaseService<UserMapper, User> implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Value("${host.address}")
    private String hostAddress;

    /**
     * 用户注册
     */
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        // 校验必要的参数
        if (isUsernameExist(request.getUsername())) {
            throw ServiceException.conflict("用户名已存在");
        }
        if (isEmailExist(request.getEmail())) {
            throw ServiceException.conflict("邮箱已存在");
        }

        // 邮箱验证码
        String mailKey = String.format(C.KEY_MAIL_CODE, request.getEmail());
        String code = getAndDeleteStringFromRedis(mailKey);
        requireEqual(code, request.getCode(), "邮箱验证码错误");

        // 注册
        User user = request.createUser(passwordEncoder);
        save(user);

        // 登录
        return login(request.getUsername(), request.getPassword());
    }

    /**
     * 检查用户名是否存在
     */
    public boolean isUsernameExist(String username) {
        if (!StringUtils.hasText(username)) return false;
        username = URLDecoder.decode(username, StandardCharsets.UTF_8);
        return exists(getBaseMapper().queryByUser(username));
    }

    /**
     * 检查密码是否存在
     */
    public boolean isEmailExist(String email) {
        if (!StringUtils.hasText(email)) return false;
        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        return exists(getBaseMapper().queryByEmail(email));
    }

    /**
     * 发送邮箱验证码
     */
    public void sendMailCode(String email) {
        // 生成随机验证码
        String redisKey = String.format(C.KEY_MAIL_CODE, email);
        String code = StringUtils.generateRandomString(6);
        putToRedis(redisKey, code, 10);

        // 发送邮件
        String content = String.format(C.MESSAGE_SEND_MAIL_CODE, code);
        String receiver = URLDecoder.decode(email, StandardCharsets.UTF_8);
        NotificationEvent event = NotificationEvent.mail("Pet Adoption 邮箱验证码", content, receiver);
        eventPublisher.publishEvent(event);
    }

    /**
     * 登录
     */
    public UserResponse login(String username, String password) {
        CustomUserDetails principal;
        try {
            // 登录
            Authentication token = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(token);
            principal = (CustomUserDetails) authentication.getPrincipal();
        } catch (BadCredentialsException e) {
            throw ServiceException.invalidate("用户名或密码错误", e);
        } catch (AccountStatusException e) {
            throw ServiceException.auth("账号状态异常，请联系工作人员", e);
        } catch (Exception e) {
            throw ServiceException.request("登录失败: " + e.getMessage(), e);
        }

        requireExist(principal, "用户不存在");
        assert principal != null;

        // 附着 JWT 信息
        User user = principal.getUser();
        UserResponse response = UserResponse.fromEntity(user);
        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    /**
     * 获取用户信息
     */
    public UserResponse getUser(Long userId) {
        User user = requireById(userId, "用户不存在");
        return UserResponse.fromEntity(user);
    }

    /**
     * 获取用户名与头像，用于显示
     */
    public UsernameAndAvatarResponse getUsernameAndAvatar(Long userId) {
        User user = requireOne(getBaseMapper().queryUsernameAndAvatar(userId), "用户不存在");
        return UsernameAndAvatarResponse.fromEntity(user);
    }

    /**
     * 获取用户信息
     */
    public UserResponse getUser(String username) {
        User user = requireOne(User::getUsername, username, "用户不存在");
        return UserResponse.fromEntity(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void removeUser(Long userId) {
        // 查找用户
        User user = requireById(userId, "用户不存在");

        // 权限校验
        User login = AuthUtils.getLoginUser();
        boolean allowed =
                // 用户本人
                Objects.equals(login.getId(), user.getId()) ||
                // 工作人员，且被删用户非管理员
                (login.isWorker() && !user.isAdmin()) ||
                // 超级管理员
                login.isAdmin();
        requirePermission(allowed);

        // 删除
        removeById(userId);
    }

    /**
     * 获取所有用户
     */
    public Page<UserResponse> getAllUsers(Page<User> page) {
        // 权限校验
        User login = AuthUtils.getLoginUser();
        requirePermission(login.isWorker());
        // 数据转换
        Page<User> result = page(page);
        return DbUtils.convertDto(result, UserResponse::fromEntity);
    }

    /**
     * 忘记密码 - 发送密码重置链接
     */
    public void forgetPassword(String email) {
        // 查找用户
        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        User user = requireOne(User::getEmail, email, "用户不存在");

        // 生成随机密码，保存相关信息
        // 有效期 10min
        String randomId = StringUtils.randomUUID(KEY_PASSWORD_RESET, redisTemplateString, 10);
        String redisKey = String.format(KEY_PASSWORD_RESET, randomId);
        putToRedis(redisKey, user.getEmail(), 10);

        // 发送激活邮件
        String content = String.format(MESSAGE_RESET_PWD, hostAddress, randomId);
        NotificationEvent event = NotificationEvent.mail("Pet Adoption 密码重置", content, user.getEmail());
        eventPublisher.publishEvent(event);
    }

    /**
     * 忘记密码 - 密码重置
     */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        // 验证链接校验
        String redisKey = String.format(KEY_PASSWORD_RESET, request.getId());
        String email = getStringFromRedis(redisKey);
        requireExist(email, "链接已过期");

        // 更新密码
        User user = requireOne(User::getEmail, email, "用户不存在");
        request.apply(user, passwordEncoder);
        updateById(user);
        deleteStringFromRedis(redisKey);
    }

    /**
     * 修改用户信息
     */
    @Transactional
    public UserResponse update(Long userId, UserUpdateRequest request) {
        // 校验用户权限
        User login = AuthUtils.getLoginUser();
        User user = requireById(userId, "用户不存在");
        // 管理员权限仅超级管理员可更改
        requirePermission(!user.isAdmin() || login.isAdmin());
        // 非管理员变更需要本人或救助站工作人员更改
        requirePermission(login.isWorker() || Objects.equals(login.getId(), userId));

        // 更新用户信息
        request.apply(user, passwordEncoder);
        updateById(user);
        return UserResponse.fromEntity(user);
    }

    /**
     * 上传用户头像
     */
    public UserResponse uploadAvatar(Long userId, MultipartFile file) {
        // 校验用户权限
        User login = AuthUtils.getLoginUser();
        User user = requireById(userId, "用户不存在");
        requirePermission(Objects.equals(login.getId(), userId) || login.isWorker());

        // 检查图片
        Pair<String, Integer> extAndType = FileUtils.getFileExtensionAndType(file);
        requireEqual(C.MEDIA_TYPE_IMAGE, extAndType.getSecond(), "不支持的图片格式");

        // 上传图片
        Date now = new Date(System.currentTimeMillis());
        String filename = FileUtils.generateFilename(file.getOriginalFilename(), now, extAndType.getFirst());
        Path target = FileUtils.generateFilePath(C.PARENT_USER_AVATAR, userId);
        FileUtils.upload(file, filename, target);

        // 更新信息
        String oldAvatar = user.getAvatar();
        user.setAvatar(filename);
        updateById(user);
        UserResponse response = UserResponse.fromEntity(user);

        // 删除旧图片
        if (oldAvatar != null) {
            Path oldFile = FileUtils.generateFilePath(C.PARENT_USER_AVATAR, userId, oldAvatar);
            FileUtils.tryDeleteFile(oldFile);
        }

        return response;
    }

    /**
     * 根据 id 批量获取用户名和头像信息
     */
    public Map<Long, UsernameAndAvatarResponse> getUsernameAndAvatarBatchByIds(Set<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return list(getBaseMapper().queryUsernameAndAvatar(ids)).stream()
                .map(UsernameAndAvatarResponse::fromEntity)
                .collect(Func.toIdMap());
    }

    /**
     * 根据 id 批量获取邮件地址，仅 id 和 email 可用
     */
    public Set<User> getMailsBatchByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return Set.of();
        return new HashSet<>(list(getBaseMapper().queryIdAndEmails(ids)));
    }

    /**
     * 根据 id 批量获取邮件地址，仅 id 和 email 可用
     */
    public List<String> getMailsBatchByRoles(Set<String> roleSet) {
        return getBatchByRoles(roleSet, User::getEmail);
    }

    /**
     * 根据角色批量获取用户 id
     */
    public List<Long> getIdsBatchByRoles(Set<String> roleSet) {
        return getBatchByRoles(roleSet, User::getId);
    }

    private <T> List<T> getBatchByRoles(Set<String> roleSet, SFunction<User, T> column) {
        // 生成所需的 role
        Set<String> roles = (roleSet == null ? Set.<String>of() : roleSet).stream()
                .filter(StringUtils::hasText)
                .filter(C.ALL_ROLES::contains)
                .collect(Collectors.toSet());
        if (roles.isEmpty()) return List.of();
        Integer role = roleSet.stream()
                .map(C.MATCH_MASK_MAP::get)
                .filter(Objects::nonNull)
                .reduce(0, (i, j) -> i | j);
        role = AuthUtils.getRoleCode(role);
        return getBaseMapper().selectObjsByRole(role, column);
    }

    @Override
    @Nonnull
    public UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        return getOneOpt(getBaseMapper().queryByUser(username))
                .map(CustomUserDetails::new)
                .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));
    }
}
