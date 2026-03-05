package org.example.expert.config;

import io.jsonwebtoken.Claims;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // 테스트용 Base64 인코딩된 시크릿 키 (256bit 이상)
    private final String secretKey = Base64.getEncoder().encodeToString(
            "testSecretKeyForJwtTestingPurpose123!".getBytes()
    );

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKeyString", secretKey);
        jwtUtil.init();
    }

    @Test
    @DisplayName("토큰 생성 성공")
    public void createToken_토큰을_정상적으로_생성한다() {
        // given & when
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);

        // then
        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "));
    }

    @Test
    @DisplayName("토큰 substring 성공")
    public void substringToken_토큰을_정상적으로_파싱한다() {
        // given
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);

        // when
        String result = jwtUtil.substringToken(token);

        // then
        assertNotNull(result);
        assertFalse(result.startsWith("Bearer "));
    }

    @Test
    @DisplayName("토큰 substring 실패 - 유효하지 않은 토큰")
    public void substringToken_유효하지_않은_토큰으로_에러가_발생한다() {
        // given
        // Bearer prefix 없는 토큰
        String invalidToken = "InvalidToken";

        // when
        ServerException exception = assertThrows(ServerException.class, () ->
                jwtUtil.substringToken(invalidToken)
        );

        // then
        assertEquals("Not Found Token", exception.getMessage());
    }

    @Test
    @DisplayName("Claims 추출 성공")
    public void extractClaims_Claims를_정상적으로_추출한다() {
        // given
        // 토큰 생성 후 Bearer prefix 제거
        String token = jwtUtil.substringToken(
                jwtUtil.createToken(1L, "test@test.com", UserRole.USER)
        );

        // when
        Claims claims = jwtUtil.extractClaims(token);

        // then
        assertNotNull(claims);
        assertEquals("1", claims.getSubject());
        assertEquals("test@test.com", claims.get("email"));
    }

    @Test
    @DisplayName("토큰 substring 실패 - 토큰이 null")
    public void substringToken_토큰이_null이어서_에러가_발생한다() {
        // given
        // null 토큰
        // when
        ServerException exception = assertThrows(ServerException.class, () ->
                jwtUtil.substringToken(null)
        );

        // then
        assertEquals("Not Found Token", exception.getMessage());
    }

    @Test
    @DisplayName("토큰 substring 실패 - 토큰이 빈 문자열")
    public void substringToken_토큰이_빈_문자열이어서_에러가_발생한다() {
        // given
        // 빈 문자열 토큰
        // when
        ServerException exception = assertThrows(ServerException.class, () ->
                jwtUtil.substringToken("")
        );

        // then
        assertEquals("Not Found Token", exception.getMessage());
    }
}