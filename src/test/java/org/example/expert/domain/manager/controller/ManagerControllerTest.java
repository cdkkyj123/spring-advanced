package org.example.expert.domain.manager.controller;

import io.jsonwebtoken.Claims;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {

    @Mock
    private ManagerService managerService;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private ManagerController managerController;

    private final AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
    private final ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(2L);
    private final UserResponse userResponse = new UserResponse(2L, "manager@test.com");

    @Test
    @DisplayName("담당자 등록 성공")
    public void saveManager_담당자를_정상적으로_등록한다() {
        // given
        ManagerSaveResponse managerSaveResponse = new ManagerSaveResponse(1L, userResponse);

        // 담당자 등록 성공
        given(managerService.saveManager(any(AuthUser.class), anyLong(), any(ManagerSaveRequest.class)))
                .willReturn(managerSaveResponse);

        // when
        ResponseEntity<ManagerSaveResponse> result = managerController.saveManager(authUser, 1L, managerSaveRequest);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(managerSaveResponse.getId(), result.getBody().getId());
        assertEquals(userResponse.getEmail(), result.getBody().getUser().getEmail());
    }

    @Test
    @DisplayName("담당자 등록 실패 - Todo 없음")
    public void saveManager_Todo가_없어_에러가_발생한다() {
        // given
        // To-do 없음
        given(managerService.saveManager(any(AuthUser.class), anyLong(), any(ManagerSaveRequest.class)))
                .willThrow(new InvalidRequestException("Todo not found"));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerController.saveManager(authUser, 1L, managerSaveRequest)
        );

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("담당자 목록 조회 성공")
    public void getMembers_담당자_목록을_정상적으로_조회한다() {
        // given
        List<ManagerResponse> managerResponses = List.of(new ManagerResponse(1L, userResponse));

        // 담당자 목록 조회 성공
        given(managerService.getManagers(anyLong())).willReturn(managerResponses);

        // when
        ResponseEntity<List<ManagerResponse>> result = managerController.getMembers(1L);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(userResponse.getEmail(), result.getBody().get(0).getUser().getEmail());
    }

    @Test
    @DisplayName("담당자 목록 조회 실패 - Todo 없음")
    public void getMembers_Todo가_없어_에러가_발생한다() {
        // given
        // To-do 없음
        given(managerService.getManagers(anyLong()))
                .willThrow(new InvalidRequestException("Todo not found"));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerController.getMembers(1L)
        );

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("담당자 삭제 성공")
    public void deleteManager_담당자를_정상적으로_삭제한다() {
        // given
        Claims claims = mock(Claims.class);

        // JWT 토큰에서 userId 추출, 담당자 삭제 성공
        given(jwtUtil.extractClaims(anyString())).willReturn(claims);
        given(claims.getSubject()).willReturn("1");
        doNothing().when(managerService).deleteManager(anyLong(), anyLong(), anyLong());

        // when
        managerController.deleteManager("Bearer testToken", 1L, 1L);

        // then
        verify(managerService, times(1)).deleteManager(1L, 1L, 1L);
    }

    @Test
    @DisplayName("담당자 삭제 실패 - 해당 일정의 담당자 아님")
    public void deleteManager_해당_일정의_담당자가_아니어서_에러가_발생한다() {
        // given
        Claims claims = mock(Claims.class);

        // JWT 토큰에서 userId 추출, 해당 일정의 담당자 아님
        given(jwtUtil.extractClaims(anyString())).willReturn(claims);
        given(claims.getSubject()).willReturn("1");
        doThrow(new InvalidRequestException("해당 일정에 등록된 담당자가 아닙니다."))
                .when(managerService).deleteManager(anyLong(), anyLong(), anyLong());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerController.deleteManager("Bearer testToken", 1L, 1L)
        );

        // then
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }
}