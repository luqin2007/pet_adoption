package com.example.backend.service;

import com.example.backend.dto.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.property.UserRole;
import com.example.backend.event.MailSendEvent;
import com.example.backend.mapper.UserMapper;
import com.example.backend.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class UserServiceUnitTest {

    private PasswordEncoder passwordEncoder;
    private JwtHelper jwtHelper;
    private ObjectProvider<AuthenticationManager> authenticationManagerProvider;
    private AuthenticationManager authenticationManager;
    private RedisHelper redisHelper;
    private ApplicationEventPublisher eventPublisher;
    private LangHelper langHelper;
    private UserService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = mock(PasswordEncoder.class);
        jwtHelper = mock(JwtHelper.class);
        authenticationManagerProvider = mock();
        authenticationManager = mock(AuthenticationManager.class);
        redisHelper = mock(RedisHelper.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        langHelper = mock(LangHelper.class);

        when(authenticationManagerProvider.getObject()).thenReturn(authenticationManager);
        service = new UserService(passwordEncoder, jwtHelper, authenticationManagerProvider);
        service.setObjects(redisHelper, eventPublisher, mock(), langHelper, mock());
        ReflectionTestUtils.setField(service, "mailKeyTemplate", "mail:%s");
        ReflectionTestUtils.setField(service, "codeTimeout", 10L);
        ReflectionTestUtils.setField(service, "hostAddress", "http://127.0.0.1:8080");
        ReflectionTestUtils.setField(service, "pwdKeyTemplate", "pwd:%s");
        ReflectionTestUtils.setField(service, "baseMapper", mock(UserMapper.class));
    }

    @Test
    void loginReturnsUserResponseWithAccessAndRefreshTokens() {
        User user = user(11L, "alice", UserRole.NORMAL.getMask());
        Authentication authenticated = new TestingAuthenticationToken(new CustomUserDetails(user), null);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticated);
        when(jwtHelper.generateAccessToken(user)).thenReturn("access-token");
        when(jwtHelper.generateRefreshToken(user)).thenReturn("refresh-token");

        UserResponse response = service.login("alice", "secret");

        assertEquals(11L, response.getId());
        assertEquals("alice", response.getUsername());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(authenticationManager).authenticate(argThat(token ->
                "alice".equals(token.getPrincipal()) && "secret".equals(token.getCredentials())));
    }

    @Test
    void loginWrapsBadCredentialsAsBusinessInvalidateError() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.login("alice", "wrong"));

        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
        assertEquals("exception.invalidate.user.bad_credentials", ex.getMessage());
        verify(jwtHelper, never()).generateAccessToken(any());
    }

    @Test
    void sendMailCodeStoresCodeAndPublishesMailEvent() {
        when(langHelper.get("mail.send_mail_code.title")).thenReturn("验证码");
        when(langHelper.get(eq("mail.send_mail_code.content"), anyString())).thenAnswer(invocation ->
                "验证码：" + invocation.getArgument(1));

        service.sendMailCode("alice%40example.com");

        verify(redisHelper).putString(eq("mail:alice%40example.com"), argThat(code ->
                code.length() == 6 && code.chars().allMatch(ch -> Character.isLetterOrDigit((char) ch))), eq(10L));
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertInstanceOf(MailSendEvent.class, eventCaptor.getValue());
        MailSendEvent mail = (MailSendEvent) eventCaptor.getValue();
        assertTrue(mail.emails().contains("alice@example.com"));
        assertEquals("验证码", mail.title());
        assertTrue(mail.content().startsWith("验证码："));
    }

    private static User user(Long id, String username, int role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("{noop}pwd");
        user.setEmail(username + "@example.com");
        user.setRole(role);
        return user;
    }
}
