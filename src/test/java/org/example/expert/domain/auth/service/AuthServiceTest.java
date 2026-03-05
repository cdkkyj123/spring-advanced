package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    // 공통 픽스처
    private final SignupRequest signupRequest = new SignupRequest("test@test.com", "Password1234!", "USER");
    private final SigninRequest signinRequest = new SigninRequest("test@test.com", "Password1234!");
    private final User savedUser = new User("test@test.com", "encodedPassword", UserRole.USER);

    @Test
    @DisplayName("회원가입 성공")
    public void signup_회원가입을_정상적으로_완료한다() {
        // given
        ReflectionTestUtils.setField(savedUser, "id", 1L);

        // 이메일 중복 없음, 비밀번호 암호화, DB에 유저 저장, 토큰 생성
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtUtil.createToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUserRole())
        ).willReturn("Bearer testToken");

        // when
        SignupResponse result = authService.signup(signupRequest);

        // then
        assertNotNull(result);
        assertEquals("Bearer testToken", result.getBearerToken());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    public void signup_이메일이_중복되어_에러가_발생한다() {
        // given
        // 이메일 중복
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authService.signup(signupRequest)
        );

        // then
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그인 성공")
    public void signin_로그인을_정상적으로_완료한다() {
        // given
        ReflectionTestUtils.setField(savedUser, "id", 1L);

        // 유저 조회 성공, 비밀번호 일치, 토큰 생성
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtUtil.createToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUserRole())
        ).willReturn("Bearer testToken");

        // when
        SigninResponse result = authService.signin(signinRequest);

        // then
        assertNotNull(result);
        assertEquals("Bearer testToken", result.getBearerToken());
    }

    @Test
    @DisplayName("로그인 실패 - 가입되지 않은 유저")
    public void signin_가입되지_않은_유저로_에러가_발생한다() {
        // given
        // 유저 없음
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authService.signin(signinRequest)
        );

        // then
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    public void signin_비밀번호가_일치하지_않아_에러가_발생한다() {
        // given
        // 유저 조회 성공, 비밀번호 불일치
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when
        AuthException exception = assertThrows(AuthException.class, () ->
                authService.signin(signinRequest)
        );

        // then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }
}