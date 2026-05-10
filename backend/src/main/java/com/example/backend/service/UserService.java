package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.User;
import com.example.backend.entity.property.UserRole;
import com.example.backend.event.MailSendEvent;
import com.example.backend.mapper.UserMapper;
import com.example.backend.util.*;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Objects;
import java.util.Set;

import static com.example.backend.entity.property.ParentType.USER;

/**
 * 用户管理
 */
@Service
@RequiredArgsConstructor
public class UserService extends BaseService<UserMapper, User> implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final JwtHelper jwtHelper;
    private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;

    private FileService fileService;

    @Value("${host.address}")
    private String hostAddress;
    @Value("${application.code_timeout}")
    private Long codeTimeout;
    @Value("${application.mail_timeout}")
    private Long mailTimeout;
    @Value("${key.mail_code}")
    private String mailKeyTemplate;
    @Value("${key.mail_code_limit}")
    private String mailLimitKeyTemplate;
    @Value("${key.password_reset}")
    private String pwdKeyTemplate;

    /**
     * 用户注册
     */
    @Transactional
    public UserResponse register(UserRegisterTable request) {
        // 校验必要的参数
        if (isUsernameExist(request.getUsername())) {
            throw ServiceException.conflict("exception.conflict.username_exists");
        }
        if (isEmailExist(request.getEmail())) {
            throw ServiceException.conflict("exception.conflict.email_exists");
        }

        // 邮箱验证码
        String code = redisHelper.getAndDeleteString(mailKeyTemplate, request.getEmail());
        requireEqual(code, request.getCode(), "exception.invalidate.user.email_code");

        // 注册
        User user = request.createUser(passwordEncoder);
        save(user);

        // 上传头像
        MultipartFile avatar = request.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            String filename = fileService.uploadImage(avatar, user.getId(), USER);
            getBaseMapper().updateAvatar(user.getId(), filename).update();
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
        return getBaseMapper().queryByUser(username).exists();
    }

    /**
     * 检查密码是否存在
     */
    public boolean isEmailExist(String email) {
        if (!StringUtils.hasText(email)) return false;
        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        return getBaseMapper().queryByEmail(email).exists();
    }

    /**
     * 发送邮箱验证码
     */
    public void sendMailCode(String email) {
        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        if (isEmailExist(email)) { // 存在检查
            throw ServiceException.invalidate("邮箱已存在");
        }
        if (redisHelper.hasString(mailLimitKeyTemplate, email)) { // 发送检查
            throw ServiceException.invalidate("邮件过于频繁");
        }
        redisHelper.putString(String.format(mailLimitKeyTemplate, email), "", mailTimeout);

        // 生成随机验证码
        String redisKey = String.format(mailKeyTemplate, email);
        String code = StringUtils.generateRandomString(6);
        redisHelper.putString(redisKey, code, codeTimeout);

        // 发送邮件
        eventPublisher.publishEvent(new MailSendEvent(
                Set.of(email),
                langHelper.get("mail.send_mail_code.title"),
                langHelper.get("mail.send_mail_code.content", code)));
    }

    /**
     * 登录
     */
    public UserResponse login(String username, String password) {
        CustomUserDetails principal;
        try {
            // 登录
            Authentication token = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationManager authenticationManager = authenticationManagerProvider.getObject();
            Authentication authentication = authenticationManager.authenticate(token);
            principal = (CustomUserDetails) authentication.getPrincipal();
        } catch (BadCredentialsException e) {
            throw ServiceException.invalidate("exception.invalidate.user.bad_credentials", e);
        } catch (AccountStatusException e) {
            throw ServiceException.auth("exception.auth.user.status_abnormal", e);
        } catch (Exception e) {
            throw ServiceException.system("exception.system.user.login_failed", e);
        }

        requireExist(principal, "exception.not_found.user");
        assert principal != null;

        User user = principal.getUser();
        UserResponse response = UserResponse.create(user);
        bindToken(user, response);
        return response;
    }

    /**
     * 获取用户信息
     */
    public UserResponse getUser(Long userId) {
        User user = requireById(userId);
        return UserResponse.create(user);
    }

    /**
     * 获取用户信息
     */
    public UserResponse getUserWithToken(String username) {
        User user = baseMapper.queryByUser(username).require();
        UserResponse response = UserResponse.create(user);
        bindToken(user, response);
        return response;
    }

    /*
    附着 JWT 信息
     */
    private void bindToken(User user, UserResponse response) {
        String accessToken = jwtHelper.generateAccessToken(user);
        String refreshToken = jwtHelper.generateRefreshToken(user);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void removeUser(Long userId) {
        // 查找用户
        User user = requireById(userId);

        // 权限校验
        User login = requireLoginUser();
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
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        // 数据转换
        Page<User> result = page(page.createPage());
        return convertDto(result, UserResponse::create);
    }

    /**
     * 忘记密码 - 发送密码重置链接
     */
    public void forgetPassword(String email) {
        // 查找用户
        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        User user = baseMapper.queryByEmail(email).require();

        // 发送激活邮件
        // 有效期 10min
        String uuid = beginRedisUuid(pwdKeyTemplate, user.getEmail());
        eventPublisher.publishEvent(new MailSendEvent(Set.of(user.getEmail()),
                langHelper.get("mail.reset_password.title"),
                langHelper.get("mail.reset_password.content", hostAddress, uuid)));
    }

    /**
     * 忘记密码 - 密码重置
     */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        // 验证链接校验
        String email = redisHelper.getAndDeleteString(pwdKeyTemplate, request.getId());
        requireExist(email, "exception.invalidate.user.reset_link_expired");

        // 更新密码
        User user = baseMapper.queryByEmail(email).require();
        request.applyTo(user, passwordEncoder);
        updateById(user);
    }

    /**
     * 修改用户信息
     */
    @Transactional
    public UserResponse update(Long userId, UserUpdateRequest request) {
        // 校验用户权限
        User user = requireById(userId);
        requestUserPermission(user, userId);
        if (request.getRole() != null && UserRole.ADMIN.match(request.getRole())) {
            requirePermission(requireLoginUser().isAdmin());
        }

        // 更新用户信息
        request.applyTo(user, passwordEncoder);
        updateById(user);
        return UserResponse.create(user);
    }

    /**
     * 上传用户头像
     */
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 校验用户权限
        User user = requireById(userId,
                User::getId, User::getRole, User::getAvatar);
        requestUserPermission(user, userId);

        // 上传图片
        String filename = fileService.uploadImage(file, userId, USER);

        // 更新信息
        String oldAvatar = user.getAvatar();
        baseMapper.updateAvatar(userId, filename).update();

        // 删除旧图片
        fileService.deleteFile(oldAvatar, userId, USER);
        return FileUtils.generateAssetUrl(USER, userId, filename);
    }

    /**
     * 删除用户头像
     */
    @Transactional
    public void deleteAvatar(Long userId) {
        User user = requireById(userId,
                User::getId, User::getRole, User::getAvatar);
        requestUserPermission(user, userId);

        String avatar = user.getAvatar();
        baseMapper.updateAvatar(userId, null).update();
        fileService.deleteFile(avatar, userId, USER);
    }

    private void requestUserPermission(User user, Long userId) {
        User login = requireLoginUser();
        // 管理员权限仅超级管理员可更改
        requirePermission(!user.isAdmin() || login.isAdmin());
        // 非管理员变更需要本人或救助站工作人员更改
        requirePermission(login.is(userId) || login.isWorker());
    }

    @Autowired
    public void setServices(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    @Nonnull
    public UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        return getBaseMapper().queryByUser(username).opt()
                .map(CustomUserDetails::new)
                .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));
    }
}
