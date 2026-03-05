package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    private final User user = new User("test@test.com", "Password1234!", UserRole.USER);
    private final UserChangePasswordRequest userChangePasswordRequest =
            new UserChangePasswordRequest("Password1234!", "NewPassword1234!");

    @Test
    @DisplayName("유저 조회 성공")
    public void getUser_유저를_정상적으로_조회한다() {
        // given
        ReflectionTestUtils.setField(user, "id", 1L);

        // 유저 조회 성공
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        UserResponse result = userService.getUser(1L);

        // then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("유저 조회 실패 - 유저 없음")
    public void getUser_유저가_없어_에러가_발생한다() {
        // given
        // 유저 없음
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userService.getUser(1L)
        );

        // then
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    public void changePassword_비밀번호를_정상적으로_변경한다() {
        // given
        // 유저 조회 성공, 새 비밀번호 기존과 다름, 기존 비밀번호 일치, 새 비밀번호 암호화
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(anyString())).willReturn("EncodedNewPassword1234!");

        // when
        userService.changePassword(1L, userChangePasswordRequest);

        // then
        verify(passwordEncoder).encode(userChangePasswordRequest.getNewPassword());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 유저 없음")
    public void changePassword_유저가_없어_에러가_발생한다() {
        // given
        // 유저 없음
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userService.changePassword(1L, userChangePasswordRequest)
        );

        // then
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호가 기존 비밀번호와 동일")
    public void changePassword_새_비밀번호가_기존과_같아_에러가_발생한다() {
        // given
        // 유저 조회 성공, 새 비밀번호가 기존과 동일
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())).willReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userService.changePassword(1L, userChangePasswordRequest)
        );

        // then
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호 불일치")
    public void changePassword_기존_비밀번호가_달라_에러가_발생한다() {
        // given
        // 유저 조회 성공, 새 비밀번호 기존과 다름, 기존 비밀번호 불일치
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())).willReturn(false);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userService.changePassword(1L, userChangePasswordRequest)
        );

        // then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }
}