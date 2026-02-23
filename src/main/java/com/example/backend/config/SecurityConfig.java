package com.example.backend.config;

import com.example.backend.util.AccessDeniedExceptionHandler;
import com.example.backend.util.AuthenticationExceptionPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationExceptionPoint authenticationExceptionPoint,
                                           AccessDeniedExceptionHandler accessDeniedExceptionHandler) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                // 页面权限
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**",
                                "/user/login", "/user/register",  // 登录、注册
                                "/user/check/**",                   // 注册校验
                                "/user/forget", "/user/reset"     // 忘记密码
                                ).permitAll()
                        .anyRequest().authenticated())
                // 登录
                .formLogin(form -> form
                        .loginProcessingUrl("/user/login")
                        .permitAll())
                // 登出
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                // 异常处理
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationExceptionPoint)
                        .accessDeniedHandler(accessDeniedExceptionHandler))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }
}
