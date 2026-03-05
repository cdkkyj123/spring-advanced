package org.example.expert.domain.auth.controller;

import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @InjectMocks
    private AuthController authController;

    private final SignupRequest signupRequest = new SignupRequest("test@test.com", "Password1234!", "USER");
    private final SigninRequest signinRequest = new SigninRequest("test@test.com", "Password1234!");

    @Test
    @DisplayName("회원가입 성공")
    public void signup_회원가입을_정상적으로_완료한다() {
        // given
        SignupResponse signupResponse = new SignupResponse("Bearer testToken");

        // 회원가입 성공
        given(authService.signup(any(SignupRequest.class))).willReturn(signupResponse);

        // when
        SignupResponse result = authController.signup(signupRequest);

        // then
        assertNotNull(result);
        assertEquals("Bearer testToken", result.getBearerToken());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    public void signup_이메일이_중복되어_에러가_발생한다() {
        // given
        // 이메일 중복
        given(authService.signup(any(SignupRequest.class)))
                .willThrow(new InvalidRequestException("이미 존재하는 이메일입니다."));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authController.signup(signupRequest)
        );

        // then
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그인 성공")
    public void signin_로그인을_정상적으로_완료한다() {
        // given
        SigninResponse signinResponse = new SigninResponse("Bearer testToken");

        // 로그인 성공
        given(authService.signin(any(SigninRequest.class))).willReturn(signinResponse);

        // when
        SigninResponse result = authController.signin(signinRequest);

        // then
        assertNotNull(result);
        assertEquals("Bearer testToken", result.getBearerToken());
    }

    @Test
    @DisplayName("로그인 실패 - 가입되지 않은 유저")
    public void signin_가입되지_않은_유저로_에러가_발생한다() {
        // given
        // 가입되지 않은 유저
        given(authService.signin(any(SigninRequest.class)))
                .willThrow(new InvalidRequestException("가입되지 않은 유저입니다."));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authController.signin(signinRequest)
        );

        // then
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }
}