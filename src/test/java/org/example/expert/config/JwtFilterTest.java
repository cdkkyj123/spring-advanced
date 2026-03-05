package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        // 매 테스트마다 SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("/auth 경로는 필터 통과")
    public void doFilterInternal_auth경로는_필터를_통과한다() throws Exception {
        // given
        // /auth 경로 요청
        given(request.getRequestURI()).willReturn("/auth/signup");

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        // jwtUtil 호출 없이 필터 통과
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).substringToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("인증 헤더 누락 시 SecurityContext에 인증 정보 저장 안됨")
    public void doFilterInternal_인증_헤더가_없으면_인증정보가_저장되지_않는다() throws Exception {
        // given
        // Authorization 헤더 없음
        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn(null);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        // substringToken 호출 없이 필터 통과, 인증 정보 저장 안됨
        verify(jwtUtil, never()).substringToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("정상 토큰으로 SecurityContext에 인증 정보 저장됨")
    public void doFilterInternal_정상_토큰으로_인증정보가_저장된다() throws Exception {
        // given
        DefaultClaims claims = new DefaultClaims();
        claims.setSubject("1");
        claims.put("email", "test@test.com");
        claims.put("userRole", "USER");

        // 정상 토큰, claims 추출 성공
        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn("Bearer testToken");
        given(jwtUtil.substringToken(anyString())).willReturn("testToken");
        given(jwtUtil.extractClaims(anyString())).willReturn(claims);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        // SecurityContext에 인증 정보 저장됨
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("claims가 null이면 SecurityContext에 인증 정보 저장 안됨")
    public void doFilterInternal_claims가_null이면_인증정보가_저장되지_않는다() throws Exception {
        // given
        // claims null 반환
        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn("Bearer testToken");
        given(jwtUtil.substringToken(anyString())).willReturn("testToken");
        given(jwtUtil.extractClaims(anyString())).willReturn(null);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        // SecurityContext에 인증 정보 저장 안됨
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰 만료 시 SecurityContext에 인증 정보 저장 안됨")
    public void doFilterInternal_토큰이_만료되면_인증정보가_저장되지_않는다() throws Exception {
        // given
        DefaultClaims claims = new DefaultClaims();
        claims.setSubject("1");

        // 만료된 토큰
        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn("Bearer expiredToken");
        given(jwtUtil.substringToken(anyString())).willReturn("expiredToken");
        given(jwtUtil.extractClaims(anyString()))
                .willThrow(new ExpiredJwtException(null, claims, "Token expired"));

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        // SecurityContext에 인증 정보 저장 안됨
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("JWT 검증 실패 시 SecurityContext에 인증 정보 저장 안됨")
    public void doFilterInternal_JWT_검증에_실패하면_인증정보가_저장되지_않는다() throws Exception {
        // given
        // JWT 검증 실패
        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn("Bearer invalidToken");
        given(jwtUtil.substringToken(anyString())).willReturn("invalidToken");
        given(jwtUtil.extractClaims(anyString()))
                .willThrow(new MalformedJwtException("Invalid token"));

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        // SecurityContext에 인증 정보 저장 안됨
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("예상치 못한 오류 발생 시 SecurityContext에 인증 정보 저장 안됨")
    public void doFilterInternal_예상치_못한_오류가_발생하면_인증정보가_저장되지_않는다() throws Exception {
        // given
        // 예상치 못한 오류
        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn("Bearer testToken");
        given(jwtUtil.substringToken(anyString())).willReturn("testToken");
        given(jwtUtil.extractClaims(anyString()))
                .willThrow(new RuntimeException("Unexpected error"));

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        // SecurityContext에 인증 정보 저장 안됨
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}