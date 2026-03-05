package org.example.expert.domain.user.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;

    private final AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
    private final UserChangePasswordRequest userChangePasswordRequest =
            new UserChangePasswordRequest("Password1234!", "NewPassword1234!");

    @Test
    @DisplayName("유저 조회 성공")
    public void getUser_유저를_정상적으로_조회한다() {
        // given
        UserResponse userResponse = new UserResponse(1L, "test@test.com");

        // 유저 조회 성공
        given(userService.getUser(anyLong())).willReturn(userResponse);

        // when
        ResponseEntity<UserResponse> result = userController.getUser(1L);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(userResponse.getId(), result.getBody().getId());
        assertEquals(userResponse.getEmail(), result.getBody().getEmail());
    }

    @Test
    @DisplayName("유저 조회 실패 - 유저 없음")
    public void getUser_유저가_없어_에러가_발생한다() {
        // given
        // 유저 없음
        given(userService.getUser(anyLong()))
                .willThrow(new InvalidRequestException("User not found"));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userController.getUser(1L)
        );

        // then
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    public void changePassword_비밀번호를_정상적으로_변경한다() {
        // given
        // 비밀번호 변경 성공
        doNothing().when(userService).changePassword(anyLong(), any(UserChangePasswordRequest.class));

        // when
        userController.changePassword(authUser, userChangePasswordRequest);

        // then
        verify(userService, times(1)).changePassword(authUser.getId(), userChangePasswordRequest);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 유저 없음")
    public void changePassword_유저가_없어_에러가_발생한다() {
        // given
        // 유저 없음
        doThrow(new InvalidRequestException("User not found"))
                .when(userService).changePassword(anyLong(), any(UserChangePasswordRequest.class));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userController.changePassword(authUser, userChangePasswordRequest)
        );

        // then
        assertEquals("User not found", exception.getMessage());
    }
}