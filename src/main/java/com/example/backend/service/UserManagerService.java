package com.example.backend.service;

import com.example.backend.bean.PasswordResetRequest;
import com.example.backend.bean.UserLoginRequest;
import com.example.backend.bean.UserRegisterRequest;
import com.example.backend.bean.UserUpdateRequest;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import com.example.backend.util.UserUtils;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
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
    private final MailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;

    private static final String PASSWORD_RESET_KEY_TEMPLATE = "pet_adoption.forgetpwd.%s";
    private static final String PASSWORD_CODE_KEY_TEMPLATE = "pet_adoption.mailcode.%s";

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
        return userMapper.findIdByUsername(username) != null;
    }

    /**
     * 检查密码是否存在
     */
    public boolean isEmailExist(String email) {
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
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("lqjhzp@163.com");
        mail.setTo(email);
        mail.setSubject("Pet Adoption 邮箱验证码");
        mail.setText(code + "\n验证码 10min 内有效");
        try {
            mailSender.send(mail);
        } catch (Exception e) {
            throw new ServiceException("邮件发送失败: " + e.getMessage(), e);
        }
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
     * 忘记密码 - 发送密码重置链接
     */
    public void forgetPassword(String email) {
        // 查找用户
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
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("lqjhzp@163.com");
        mail.setTo(user.getEmail());
        mail.setSubject("Pet Adoption 密码重置");
        mail.setText("请点击以下链接重置密码：\n" + "http://localhost:8080/user/reset?id=" + randomId + "\n链接在 10min 内有效");
        try {
            mailSender.send();
        } catch (Exception e) {
            throw new ServiceException("邮件发送失败: " + e.getMessage(), e);
        }
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
            boolean isAdminRoleChange = UserUtils.isAdmin(oldUser.getRole()) != UserUtils.isAdmin(user.getRole());
            if (isAdminRoleChange && !UserUtils.isAdmin(login.getRole())) {
                throw new ServiceException("用户权限不足");
            }
            // 其他权限变更需要救助站工作人员更改
            if (!UserUtils.isWorker(login.getRole())) {
                throw new ServiceException("用户权限不足");
            }
        }
        if (!Objects.equals(login.getId(), userId) /* 用户本身 */
                && !UserUtils.isWorker(login.getRole()) /* 救助站工作人员 */) {
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
