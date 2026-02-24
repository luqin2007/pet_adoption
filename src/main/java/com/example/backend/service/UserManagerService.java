package com.example.backend.service;

import com.example.backend.bean.PasswordResetRequest;
import com.example.backend.bean.UserLoginRequest;
import com.example.backend.bean.UserRegisterRequest;
import com.example.backend.bean.UserUpdateRequest;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.util.*;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserManagerService implements UserDetailsService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final MailUtils mailUtils;
    private AuthenticationManager authenticationManager;

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
    public User register(UserRegisterRequest user) {
        // 校验必要的参数
        if (isUsernameExist(user.getUsername())) {
            throw new ServiceException("用户名已存在");
        }
        if (isEmailExist(user.getEmail())) {
            throw new ServiceException("邮箱已存在");
        }
        if (!isEmailCodeMatched(user.getEmail(), user.getCode())) {
            throw new ServiceException("邮箱验证码错误");
        }

        // 重置邮箱验证码
        String redisKey = String.format(PASSWORD_CODE_KEY_TEMPLATE, user.getEmail());
        redisTemplate.delete(redisKey);

        // 注册
        User userEntity = new User();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        userEntity.setEmail(user.getEmail());
        userEntity.setRole(0);
        userEntity.setAvatar(user.getAvatar());
        userEntity.setCreateTime(Date.valueOf(LocalDate.now()));
        userEntity.setUpdateTime(Date.valueOf(LocalDate.now()));
        userMapper.insert(userEntity);
        return userMapper.findById(userEntity.getId());
    }

    /**
     * 检查用户名是否存在
     */
    public boolean isUsernameExist(String username) {
        username = URLDecoder.decode(username, StandardCharsets.UTF_8);
        return userMapper.findIdByUsername(username) != null;
    }

    /**
     * 检查密码是否存在
     */
    public boolean isEmailExist(String email) {
        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        return userMapper.findIdByEmail(email) != null;
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
        mailUtils.send(receiver, "Pet Adoption 邮箱验证码", content);
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
    public User login(UserLoginRequest user) {
        CustomUserDetails principal;
        try {
            Authentication token = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
            Authentication authentication = authenticationManager.authenticate(token);
            principal = (CustomUserDetails) authentication.getPrincipal();
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
        return principal.getUser();
    }

    /**
     * 获取用户信息
     */
    public User getUser(Long id) {
        // 获取用户信息
        User user = userMapper.findById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    /**
     * 获取用户信息
     */
    public User getUser(String username) {
        // 获取用户信息
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    /**
     * 删除用户
     */
    @Transactional
    public void removeUser(Long id) {
        // 查找用户
        User user = userMapper.findById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 权限校验
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        if (principal == null) {
            throw new ServiceException("请先登录");
        }
        User login = principal.getUser();
        boolean allowed =
                // 用户本人
                Objects.equals(login.getId(), user.getId()) ||
                // 工作人员，且被删用户非管理员
                (UserUtils.isWorker(login) && !UserUtils.isAdmin(user)) ||
                // 管理员
                UserUtils.isAdmin(login);
        if (!allowed) {
            throw new ServiceException("权限不足");
        }

        // 删除
        userMapper.delete(id);
    }

    /**
     * 忘记密码 - 发送密码重置链接
     */
    public void forgetPassword(String email) {
        // 查找用户
        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

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
        mailUtils.send(user.getEmail(), "Pet Adoption 密码重置", content);
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

        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.updatePassword(user);
        redisTemplate.delete(redisKey);
    }

    /**
     * 修改用户信息
     */
    @Transactional
    public User update(Long userId, UserUpdateRequest user) {
        // 校验用户权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        if (principal == null) {
            throw new ServiceException("请先登录");
        }
        User oldUser = userMapper.findById(userId);
        User login = principal.getUser();
        if (oldUser.getRole() != user.getRole()) {
            // 管理员权限仅超级管理员可更改
            boolean isAdminRoleChange = UserUtils.isAdmin(oldUser) != UserUtils.isAdmin(user.getRole());
            if (isAdminRoleChange && !UserUtils.isAdmin(login)) {
                throw new ServiceException("用户权限不足");
            }
            // 其他权限变更需要救助站工作人员更改
            if (!UserUtils.isWorker(login)) {
                throw new ServiceException("用户权限不足");
            }
        }
        if (!Objects.equals(login.getId(), userId) /* 用户本身 */
                && !UserUtils.isWorker(login) /* 救助站工作人员 */) {
            // 其他信息只需本人或救助站工作人员即可
            throw new ServiceException("用户权限不足");
        }

        oldUser.setUsername(user.getUsername());
        oldUser.setPassword(user.getPassword());
        oldUser.setEmail(user.getEmail());
        oldUser.setRole(user.getRole());
        oldUser.setAvatar(user.getAvatar());
        oldUser.setUpdateTime(Date.valueOf(LocalDate.now()));
        userMapper.update(oldUser);
        return oldUser;
    }

    @Override
    @Nonnull
    public UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            return new CustomUserDetails(user);
        }
        throw UsernameNotFoundException.fromUsername(username);
    }

    @Autowired
    @Lazy
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
