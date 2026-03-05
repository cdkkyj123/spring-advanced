package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthUserArgumentResolverTest {

    private final AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

    @Mock
    private MethodParameter parameter;
    @Mock
    private NativeWebRequest webRequest;
    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("@Auth 어노테이션과 AuthUser 타입이 함께 사용된 경우 true 반환")
    public void supportsParameter_Auth어노테이션과_AuthUser타입이_함께_사용되면_true를_반환한다() {
        // given
        // @Auth 어노테이션 있음, AuthUser 타입 일치
        given(parameter.getParameterAnnotation(Auth.class)).willReturn(mock(Auth.class));
        given(parameter.getParameterType()).willAnswer(invocation -> AuthUser.class);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("@Auth 어노테이션만 있고 AuthUser 타입이 아닌 경우 에러 발생")
    public void supportsParameter_Auth어노테이션만_있고_AuthUser타입이_아니면_에러가_발생한다() {
        // given
        // @Auth 어노테이션 있음, AuthUser 타입 불일치
        given(parameter.getParameterAnnotation(Auth.class)).willReturn(mock(Auth.class));
        given(parameter.getParameterType()).willAnswer(invocation -> String.class);

        // when
        AuthException exception = assertThrows(AuthException.class, () ->
                resolver.supportsParameter(parameter)
        );

        // then
        assertEquals("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("@Auth 어노테이션 없고 AuthUser 타입인 경우 에러 발생")
    public void supportsParameter_Auth어노테이션_없고_AuthUser타입이면_에러가_발생한다() {
        // given
        // @Auth 어노테이션 없음, AuthUser 타입 일치
        given(parameter.getParameterAnnotation(Auth.class)).willReturn(null);
        given(parameter.getParameterType()).willAnswer(invocation -> AuthUser.class);

        // when
        AuthException exception = assertThrows(AuthException.class, () ->
                resolver.supportsParameter(parameter)
        );

        // then
        assertEquals("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("resolveArgument 성공")
    public void resolveArgument_AuthUser를_정상적으로_반환한다() {
        // given
        // request에서 userId, email, userRole 추출
        given(webRequest.getNativeRequest()).willReturn(httpServletRequest);
        given(httpServletRequest.getAttribute("userId")).willReturn(1L);
        given(httpServletRequest.getAttribute("email")).willReturn("test@test.com");
        given(httpServletRequest.getAttribute("userRole")).willReturn("USER");

        // when
        AuthUser result = (AuthUser) resolver.resolveArgument(parameter, null, webRequest, null);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@test.com", result.getEmail());
        assertEquals(UserRole.USER, result.getUserRole());
    }
}