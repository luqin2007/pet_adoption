package com.example.backend.service;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.dto.*;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.util.*;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> implements UserDetailsService {

    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtils authUtils;
    private final JwtUtils jwtUtils;
    private final ApplicationEventPublisher eventPublisher;

    private static final String PASSWORD_RESET_KEY_TEMPLATE = "pet_adoption.forgetpwd.%s";
    private static final String PASSWORD_CODE_KEY_TEMPLATE = "pet_adoption.mailcode.%s";
    private static final String MAIL_RESET_PASSWORD_TEMPLATE = "请点击以下链接重置密码：\n<a>%s/user/reset?id=%s</a>\n链接在 10min 内有效";
    private static final String MAIL_CODE_TEMPLATE = "%s\n验证码 10min 内有效";

    @Value("${host.address}")
    private String hostAddress;

    /**
     * 用户注册
     */
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        // 校验必要的参数
        if (isUsernameExist(request.getUsername())) {
            throw new ServiceException("用户名已存在");
        }
        if (isEmailExist(request.getEmail())) {
            throw new ServiceException("邮箱已存在");
        }
        if (!isEmailCodeMatched(request.getEmail(), request.getCode())) {
            throw new ServiceException("邮箱验证码错误");
        }

        // 重置邮箱验证码
        String redisKey = String.format(PASSWORD_CODE_KEY_TEMPLATE, request.getEmail());
        redisTemplate.delete(redisKey);

        // 注册
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(0);
        user.setAvatar(request.getAvatar());
        user.setCreateTime(Date.valueOf(LocalDate.now()));
        user.setUpdateTime(Date.valueOf(LocalDate.now()));
        save(user);

        // 登录
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername(request.getUsername());
        loginRequest.setPassword(request.getPassword());
        return login(loginRequest);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean isUsernameExist(String username) {
        if (!StringUtils.hasText(username)) return false;
        username = URLDecoder.decode(username, StandardCharsets.UTF_8);
        return exists(lambdaQuery().eq(User::getUsername, username));
    }

    /**
     * 检查密码是否存在
     */
    public boolean isEmailExist(String email) {
        if (!StringUtils.hasText(email)) return false;
        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        return exists(lambdaQuery().eq(User::getEmail, email));
    }

    /**
     * 发送邮箱验证码
     */
    public void sendMailCode(String email) {
        // 生成随机验证码
        String redisKey = String.format(PASSWORD_CODE_KEY_TEMPLATE, email);
        String code = StringUtils.generateRandomString(6);
        redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(10));

        // 发送邮件
        String content = String.format(MAIL_CODE_TEMPLATE, code);
        String receiver = URLDecoder.decode(email, StandardCharsets.UTF_8);
        NotificationEvent event = NotificationEvent.mails("Pet Adoption 邮箱验证码", content, Set.of(receiver));
        eventPublisher.publishEvent(event);
    }

    /**
     * 检查邮箱验证码
     */
    public boolean isEmailCodeMatched(String email, String code) {
        String redisKey = String.format(PASSWORD_CODE_KEY_TEMPLATE, email);
        return Objects.equals(redisTemplate.opsForValue().get(redisKey), code);
    }

    /**
     * 登录
     */
    public UserResponse login(UserLoginRequest request) {
        CustomUserDetails principal;
        try {
            principal = authUtils.authenticate(request.getUsername(), request.getPassword());
        } catch (BadCredentialsException e) {
            throw new ServiceException("用户名或密码错误", e);
        } catch (AccountStatusException e) {
            throw new ServiceException("账号状态异常，请联系工作人员解决", e);
        } catch (Exception e) {
            throw new ServiceException("登录失败: " + e.getMessage(), e);
        }

        if (principal == null) {
            throw new ServiceException("用户不存在");
        }

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
    public UserResponse getUser(Long userId, boolean allowNotExist) {
        if (allowNotExist) {
            User user = getById(userId);
            return user == null ? null : UserResponse.fromEntity(user);
        } else {
            return getOptById(userId)
                    .map(UserResponse::fromEntity)
                    .orElseThrow(() -> new ServiceException("用户不存在"));
        }
    }

    /**
     * 获取用户信息
     */
    public User getUser(String username) {
        return getOneOpt(lambdaQuery().eq(User::getUsername, username))
                .orElseThrow(() -> new ServiceException("用户不存在"));
    }

    /**
     * 删除用户
     */
    @Transactional
    public void removeUser(Long userId) {
        // 查找用户
        User user = getOptById(userId)
                .orElseThrow(() -> new ServiceException("用户不存在"));

        // 权限校验
        User login = authUtils.getLoginUser(ServiceException::new);
        boolean allowed =
                // 用户本人
                Objects.equals(login.getId(), user.getId()) ||
                        // 工作人员，且被删用户非管理员
                        (login.isWorker() && !user.isAdmin()) ||
                        // 超级管理员
                        login.isAdmin();
        if (!allowed) {
            throw new ServiceException("权限不足");
        }

        // 删除
        removeById(userId);
    }

    /**
     * 获取所有用户
     */
    public Page<UserResponse> getAllUsers(Page<User> page) {
        // 权限校验
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!login.isWorker()) {
            throw new ServiceException("权限不足");
        }
        // 数据转换
        Page<User> result = page(page);
        return PageUtils.convertDto(result, UserResponse::fromEntity);
    }

    /**
     * 忘记密码 - 发送密码重置链接
     */
    public void forgetPassword(String email) {
        // 查找用户
        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        User user = getOneOpt(lambdaQuery().eq(User::getEmail, email))
                .orElseThrow(() -> new ServiceException("用户不存在"));

        // 生成随机密码，保存相关信息
        // 有效期 10min
        String randomId, redisKey;
        int retryTimes = 0;
        do {
            randomId = UUID.randomUUID().toString();
            redisKey = String.format(PASSWORD_RESET_KEY_TEMPLATE, randomId);
            if (retryTimes++ > 10) {
                throw new ServiceException("请稍后重试");
            }
        } while (redisTemplate.hasKey(redisKey));
        redisTemplate.opsForValue().set(redisKey, user.getEmail(), Duration.ofMinutes(10));

        // 发送激活邮件
        String content = String.format(MAIL_RESET_PASSWORD_TEMPLATE, hostAddress, randomId);
        NotificationEvent event = NotificationEvent.mails("Pet Adoption 密码重置", content, Set.of(user.getEmail()));
        eventPublisher.publishEvent(event);
    }

    /**
     * 忘记密码 - 密码重置
     */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        String redisKey = String.format(PASSWORD_RESET_KEY_TEMPLATE, request.getId());
        String email = redisTemplate.opsForValue().get(redisKey);
        if (email == null) {
            throw new ServiceException("链接已过期");
        }

        String pwd = passwordEncoder.encode(request.getPassword());
        boolean updated = update(lambdaUpdate()
                .eq(User::getEmail, email)
                .set(User::getPassword, pwd));
        redisTemplate.delete(redisKey);
        if (!updated) {
            throw new ServiceException("用户不存在");
        }
    }

    /**
     * 修改用户信息
     */
    @Transactional
    public UserResponse update(Long userId, UserUpdateRequest request) {
        // 校验用户权限
        User login = authUtils.getLoginUser(ServiceException::new);
        User user = getById(userId);
        if (user.isAdmin() && !login.isAdmin()) {
            // 管理员权限仅超级管理员可更改
            throw new ServiceException("用户权限不足");
        }
        if (!login.isWorker() && !Objects.equals(login.getId(), userId)) {
            // 其他权限变更需要本人或救助站工作人员更改
            throw new ServiceException("用户权限不足");
        }

        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole(AuthUtils.getRoleCode(request.getRole()));
        user.setAvatar(request.getAvatar());
        user.setUpdateTime(Date.valueOf(LocalDate.now()));
        if (!updateById(user)) {
            throw new ServiceException("用户不存在");
        }
        return UserResponse.fromEntity(user);
    }

    /**
     * 根据 id 批量获取用户信息
     */
    public Map<Long, UserResponse> getUsersBatchByIds(Set<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return listByIds(ids).stream()
                .collect(Collectors.toMap(User::getId, UserResponse::fromEntity));
    }

    /**
     * 根据 id 批量获取邮件地址，用于发送邮件
     */
    public Set<String> getMailsBatchByIds(Set<Long> ids) {
        if (ids.isEmpty()) return Set.of();
        return listObjs(lambdaQuery().select(User::getEmail).in(User::getId, ids)).stream()
                .map(obj -> (String) obj)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    /**
     * 根据角色批量获取用户 id
     */
    public Set<Long> getIdsBatchByRoles(Set<String> roleSet) {
        Set<String> roles = (roleSet == null ? Set.<String>of() : roleSet).stream()
                .filter(StringUtils::hasText)
                .filter(AuthUtils.ALL_ROLES::contains)
                .collect(Collectors.toSet());
        if (roles.isEmpty()) return Set.of();
        Integer role = roleSet.stream()
                .map(AuthUtils.MATCH_MASK_MAP::get)
                .filter(Objects::nonNull)
                .reduce(0, (i, j) -> i | j);
        role = AuthUtils.getRoleCode(role);

        LambdaQueryChainWrapper<User> wrapper = lambdaQuery()
                .select(User::getId)
                .eq(User::getRole, role);
        return listObjs(wrapper)
                .stream().map(obj -> (Long) obj)
                .collect(Collectors.toSet());
    }

    @Override
    @Nonnull
    public UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        return getOneOpt(lambdaQuery().eq(User::getUsername, username))
                .map(CustomUserDetails::new)
                .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));
    }
}
