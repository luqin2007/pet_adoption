package com.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.User;
import com.example.backend.entity.property.MediaType;
import com.example.backend.entity.property.UserRole;
import com.example.backend.event.NotificationEvent;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.backend.entity.property.MediaType.IMAGE;
import static com.example.backend.entity.property.ParentType.USER;
import static com.example.backend.util.C.*;

/**
 * 用户管理
 */
@Service
@RequiredArgsConstructor
public class UserService extends BaseService<UserMapper, User> implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final JwtHelper jwtHelper;
    private final AuthenticationManager authenticationManager;

    @Value("${host.address}")
    private String hostAddress;

    /**
     * 用户注册
     */
    @Transactional
    public UserResponse register(UserRegisterTable request) {
        // 校验必要的参数
        if (isUsernameExist(request.getUsername())) {
            throw ServiceException.conflict("用户名已存在");
        }
        if (isEmailExist(request.getEmail())) {
            throw ServiceException.conflict("邮箱已存在");
        }

        // 邮箱验证码
        String code = redisHelper.getAndDeleteString(KEY_MAIL_CODE, request.getEmail());
        requireEqual(code, request.getCode(), "邮箱验证码错误");

        // 注册
        User user = request.createUser(passwordEncoder);
        save(user);

        // 上传头像
        MultipartFile avatar = request.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(avatar);
            requireEqual(extAndType.getSecond(), IMAGE, "头像格式错误");
            String name = FileUtils.getNameWithoutExtension(avatar.getOriginalFilename());
            String filename = FileUtils.generateFilename(name, user.getCreateTime(), extAndType.getFirst());
            Path path = FileUtils.generateFilePath(USER, user.getId());
            FileUtils.upload(avatar, filename, path);
            update(getBaseMapper().updateAvatar(user.getId(), filename));
        }

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
        String redisKey = String.format(KEY_MAIL_CODE, email);
        String code = StringUtils.generateRandomString(6);
        redisHelper.putString(redisKey, code, 10);

        // 发送邮件
        String content = String.format(MESSAGE_SEND_MAIL_CODE, code);
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
        String accessToken = jwtHelper.generateAccessToken(user);
        String refreshToken = jwtHelper.generateRefreshToken(user);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    /**
     * 获取用户信息
     */
    public UserResponse getUser(Long userId) {
        User user = requireById(userId);
        return UserResponse.fromEntity(user);
    }

    /**
     * 获取用户信息
     */
    public UserResponse getUser(String username) {
        User user = requireOne(baseMapper.queryByUser(username));
        return UserResponse.fromEntity(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void removeUser(Long userId) {
        // 查找用户
        User user = requireById(userId);

        // 权限校验
        User login = getLoginUser();
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
    public Page<UserResponse> getAllUsers(PageParams page) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isWorker());
        // 数据转换
        Page<User> result = page(page.createPage());
        return convertDto(result, UserResponse::fromEntity);
    }

    /**
     * 忘记密码 - 发送密码重置链接
     */
    public void forgetPassword(String email) {
        // 查找用户
        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        User user = requireOne(baseMapper.queryByEmail(email));

        // 生成随机密码，保存相关信息
        // 有效期 10min
        String randomId = StringUtils.randomUUID(KEY_PASSWORD_RESET, redisHelper, 10);
        String redisKey = String.format(KEY_PASSWORD_RESET, randomId);
        redisHelper.putString(redisKey, user.getEmail(), 10);

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
        String email = redisHelper.getAndDeleteString(KEY_PASSWORD_RESET, request.getId());
        requireExist(email, "链接已过期");

        // 更新密码
        User user = requireOne(baseMapper.queryByEmail(email));
        request.applyTo(user, passwordEncoder);
        updateById(user);
    }

    /**
     * 修改用户信息
     */
    @Transactional
    public UserResponse update(Long userId, UserUpdateRequest request) {
        // 校验用户权限
        User login = getLoginUser();
        User user = requireById(userId);
        // 管理员权限仅超级管理员可更改
        requirePermission(!user.isAdmin() || login.isAdmin());
        // 非管理员变更需要本人或救助站工作人员更改
        requirePermission(login.isWorker() || Objects.equals(login.getId(), userId));

        // 更新用户信息
        request.applyTo(user, passwordEncoder);
        updateById(user);
        return UserResponse.fromEntity(user);
    }

    /**
     * 上传用户头像
     */
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 校验用户权限
        User login = getLoginUser();
        User user = requireById(userId,
                User::getId, User::getAvatar);
        requirePermission(Objects.equals(login.getId(), userId) || login.isWorker());

        // 检查图片
        Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(file);
        requireEqual(IMAGE, extAndType.getSecond(), "不支持的图片格式");

        // 上传图片
        Date now = new Date(System.currentTimeMillis());
        String filename = FileUtils.generateFilename(file.getOriginalFilename(), now, extAndType.getFirst());
        Path target = FileUtils.generateFilePath(USER, userId);
        FileUtils.upload(file, filename, target);

        // 更新信息
        String oldAvatar = user.getAvatar();
        update(baseMapper.updateAvatar(userId, filename));

        // 删除旧图片
        if (oldAvatar != null) {
            Path oldFile = FileUtils.generateFilePath(USER, userId, oldAvatar);
            FileUtils.tryDeleteFile(oldFile);
        }

        return FileUtils.generateAssetUrl(USER, userId, filename);
    }

    /**
     * 删除用户头像
     */
    @Transactional
    public void deleteAvatar(Long userId) {
        User user = getLoginUser();
        if (StringUtils.hasText(user.getAvatar())) {
            // 删除文件
            Path path = FileUtils.generateFilePath(USER, userId, user.getAvatar());
            FileUtils.tryDeleteFile(path);

            // 更新
            user.setAvatar(null);
            update(baseMapper.updateAvatar(userId, null));
        }
    }

    /**
     * 根据 id 批量获取邮件地址，仅 id 和 email 可用
     */
    public List<String> getMailsBatchByRoles(Set<UserRole> roleSet) {
        return getBatchByRoles(roleSet, User::getEmail);
    }

    /**
     * 根据角色批量获取用户 id
     */
    public List<Long> getIdsBatchByRoles(Set<UserRole> roleSet) {
        return getBatchByRoles(roleSet, User::getId);
    }

    /*
     * 根据角色批量获取用户某列
     */
    private <T> List<T> getBatchByRoles(Set<UserRole> roleSet, SFunction<User, T> column) {
        // 生成所需的 role
        int role = Stream.ofNullable(roleSet).flatMap(Set::stream)
                .mapToInt(UserRole::getMatchMask)
                .reduce(0, (a, b) -> a | b);
        return list(baseMapper.queryByRole(role).select(column)).stream()
                .map(column)
                .toList();
    }

    @Override
    @Nonnull
    public UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        return getOneOpt(getBaseMapper().queryByUser(username))
                .map(CustomUserDetails::new)
                .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));
    }
}
