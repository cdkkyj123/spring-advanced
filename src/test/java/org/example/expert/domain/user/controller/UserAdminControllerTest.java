package org.example.expert.domain.user.controller;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminControllerTest {

    @Mock
    private UserAdminService userAdminService;
    @InjectMocks
    private UserAdminController userAdminController;

    private final UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("ADMIN");

    @Test
    @DisplayName("유저 권한 변경 성공")
    public void changeUserRole_유저_권한을_정상적으로_변경한다() {
        // given
        // 권한 변경 요청 성공
        doNothing().when(userAdminService).changeUserRole(anyLong(), any(UserRoleChangeRequest.class));

        // when
        userAdminController.changeUserRole(1L, userRoleChangeRequest);

        // then
        verify(userAdminService, times(1)).changeUserRole(1L, userRoleChangeRequest);
    }

    @Test
    @DisplayName("유저 권한 변경 실패 - 유저 없음")
    public void changeUserRole_유저가_없어_에러가_발생한다() {
        // given
        // 유저 없음
        doThrow(new InvalidRequestException("User not found"))
                .when(userAdminService).changeUserRole(anyLong(), any(UserRoleChangeRequest.class));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userAdminController.changeUserRole(1L, userRoleChangeRequest)
        );

        // then
        assertEquals("User not found", exception.getMessage());
    }
}