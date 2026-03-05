package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("InvalidRequestException 처리 성공")
    public void invalidRequestExceptionException_예외를_정상적으로_처리한다() {
        // given
        InvalidRequestException exception = new InvalidRequestException("잘못된 요청입니다.");

        // when
        ResponseEntity<Map<String, Object>> result =
                globalExceptionHandler.invalidRequestExceptionException(exception);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("BAD_REQUEST", result.getBody().get("status"));
        assertEquals(400, result.getBody().get("code"));
        assertEquals("잘못된 요청입니다.", result.getBody().get("message"));
    }

    @Test
    @DisplayName("AuthException 처리 성공")
    public void handleAuthException_예외를_정상적으로_처리한다() {
        // given
        AuthException exception = new AuthException("인증에 실패했습니다.");

        // when
        ResponseEntity<Map<String, Object>> result =
                globalExceptionHandler.handleAuthException(exception);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("UNAUTHORIZED", result.getBody().get("status"));
        assertEquals(401, result.getBody().get("code"));
        assertEquals("인증에 실패했습니다.", result.getBody().get("message"));
    }

    @Test
    @DisplayName("ServerException 처리 성공")
    public void handleServerException_예외를_정상적으로_처리한다() {
        // given
        ServerException exception = new ServerException("서버 오류가 발생했습니다.");

        // when
        ResponseEntity<Map<String, Object>> result =
                globalExceptionHandler.handleServerException(exception);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", result.getBody().get("status"));
        assertEquals(500, result.getBody().get("code"));
        assertEquals("서버 오류가 발생했습니다.", result.getBody().get("message"));
    }
}