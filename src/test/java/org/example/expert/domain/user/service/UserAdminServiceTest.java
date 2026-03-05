package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserAdminService userAdminService;

    private final User user = new User("test@test.com", "Password1234!", UserRole.USER);
    private final UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("ADMIN");

    @Test
    @DisplayName("유저 권한 변경 성공")
    public void changeUserRole_유저_권한을_정상적으로_변경한다() {
        // given
        // 유저 조회 성공, 권한 변경
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        userAdminService.changeUserRole(1L, userRoleChangeRequest);

        // then
        assertEquals(UserRole.ADMIN, user.getUserRole());
    }

    @Test
    @DisplayName("유저 권한 변경 실패 - 유저 없음")
    public void changeUserRole_유저가_없어_에러가_발생한다() {
        // given
        // 유저 없음
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userAdminService.changeUserRole(1L, userRoleChangeRequest)
        );

        // then
        assertEquals("User not found", exception.getMessage());
    }
}