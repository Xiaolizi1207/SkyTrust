package com.skytrust.service.impl;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.PasswordPolicyUtil;
import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.dto.LoginDTO;
import com.skytrust.entity.User;
import com.skytrust.mapper.UserMapper;
import com.skytrust.service.LoginLogService;
import com.skytrust.service.RoleMenuService;
import com.skytrust.service.TokenBlacklistService;
import com.skytrust.service.UserRoleService;
import com.skytrust.service.UserService;
import com.skytrust.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock private UserService userService;
    @Mock private UserMapper userMapper;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private UserRoleService userRoleService;
    @Mock private RoleMenuService roleMenuService;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private LoginLogService loginLogService;
    @Mock private PasswordPolicyUtil passwordPolicyUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private final PasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
    private User testUser;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        // 初始化 SecurityUtil（JWT 需要）
        SecurityUtil.init("test-jwt-secret-for-unit-tests-only", 7200000, 604800000);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(bcryptEncoder.encode("Test@123"));
        testUser.setPhone("13800138000");
        testUser.setEmail("test@skytrust.com");
        testUser.setStatus(1); // ENABLED
        testUser.setRole("user");

        request = new MockHttpServletRequest();
    }

    // ========== 登录成功 ==========

    @Test
    void login_shouldReturnTokensWhenCredentialsValid() {
        // PasswordEncoder mock must match the BCrypt encoded password
        when(passwordEncoder.matches(eq("Test@123"), anyString())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        doNothing().when(loginLogService).recordLogin(anyLong(), anyString(), anyInt(), anyString(), anyString(), any());

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test@123");

        Result<LoginVO> result = authService.login(dto, request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData().getAccessToken());
        assertNotNull(result.getData().getRefreshToken());
        assertEquals("testuser", result.getData().getUser().getUsername());
    }

    // ========== 登录失败：密码错误 ==========

    @Test
    void login_shouldReturnErrorWhenPasswordWrong() {
        when(passwordEncoder.matches(eq("WrongPassword"), anyString())).thenReturn(false);
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        doNothing().when(loginLogService).recordLogin(anyLong(), anyString(), anyInt(), anyString(), anyString(), any());

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("WrongPassword");

        Result<LoginVO> result = authService.login(dto, request);

        assertFalse(result.isSuccess());
        assertNotNull(result.getCode());
    }

    // ========== 登录失败：用户不存在 ==========

    @Test
    void login_shouldReturnErrorWhenUserNotFound() {
        when(userService.getUserByUsername("nonexistent")).thenReturn(null);
        when(userService.getUserByPhone("nonexistent")).thenReturn(null);
        when(userService.getUserByEmail("nonexistent")).thenReturn(null);
        doNothing().when(loginLogService).recordLogin(isNull(), anyString(), anyInt(), anyString(), anyString(), any());

        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexistent");
        dto.setPassword("anything");

        Result<LoginVO> result = authService.login(dto, request);

        assertFalse(result.isSuccess());
        assertEquals(ResultCode.USER_NOT_EXIST.getCode(), result.getCode());
    }

    // ========== 登录失败：用户被禁用 ==========

    @Test
    void login_shouldReturnErrorWhenUserDisabled() {
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        testUser.setStatus(0); // DISABLED
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        doNothing().when(loginLogService).recordLogin(anyLong(), anyString(), anyInt(), anyString(), anyString(), any());

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test@123");

        Result<LoginVO> result = authService.login(dto, request);

        assertFalse(result.isSuccess());
        assertEquals(ResultCode.USER_DISABLED.getCode(), result.getCode());
    }

    // ========== Token 刷新 ==========

    @Test
    void refreshToken_shouldReturnNewTokensWhenRefreshTokenValid() {
        String refreshToken = SecurityUtil.generateRefreshToken("testuser");
        when(tokenBlacklistService.isBlacklisted(refreshToken)).thenReturn(false);
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);

        Result<LoginVO> result = authService.refreshToken(refreshToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData().getAccessToken());
    }

    @Test
    void refreshToken_shouldReturnErrorWhenTokenBlacklisted() {
        String refreshToken = SecurityUtil.generateRefreshToken("testuser");
        when(tokenBlacklistService.isBlacklisted(refreshToken)).thenReturn(true);

        Result<LoginVO> result = authService.refreshToken(refreshToken);

        assertFalse(result.isSuccess());
    }

    @Test
    void refreshToken_shouldReturnErrorWhenTokenInvalid() {
        Result<LoginVO> result = authService.refreshToken("not-a-valid-jwt");

        assertFalse(result.isSuccess());
    }

    // ========== Token 注销 ==========

    @Test
    void logout_shouldAddTokenToBlacklist() {
        String token = SecurityUtil.generateToken("testuser");
        doNothing().when(tokenBlacklistService).addToBlacklist(eq(token), anyLong());

        boolean result = authService.logout(token);

        assertTrue(result);
        verify(tokenBlacklistService).addToBlacklist(eq(token), anyLong());
    }

    // ========== 密码验证 ==========

    @Test
    void password_shouldBeEncodedWithBCrypt() {
        String raw = "SecurePass@123";
        String encoded = bcryptEncoder.encode(raw);

        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$") || encoded.startsWith("$2y$"));
    }

    @Test
    void passwordVerification_shouldMatchCorrectPassword() {
        String raw = "MySecret@456";
        String encoded = bcryptEncoder.encode(raw);

        boolean matches = bcryptEncoder.matches(raw, encoded);
        assertTrue(matches);
    }

    @Test
    void passwordVerification_shouldRejectWrongPassword() {
        String encoded = bcryptEncoder.encode("RightOne");

        boolean matches = bcryptEncoder.matches("WrongOne", encoded);
        assertFalse(matches);
    }

    // ========== 用户名/密码空值 ==========

    @Test
    void login_shouldReturnErrorWhenUsernameNull() {
        LoginDTO dto = new LoginDTO();
        dto.setPassword("something");

        Result<LoginVO> result = authService.login(dto, request);

        assertFalse(result.isSuccess());
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
    }

    @Test
    void login_shouldReturnErrorWhenPasswordNull() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");

        Result<LoginVO> result = authService.login(dto, request);

        assertFalse(result.isSuccess());
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
    }
}
