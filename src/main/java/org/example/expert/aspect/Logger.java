package org.example.expert.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class Logger {

    // 로그를 남길 메서드 들을 지정
    @Pointcut("@annotation(org.example.expert.aspect.AdminApiLog)")
    public void adminApiLog() {}

    // 위에 지정한 메서드들의 시작 전 후
    @Around("adminApiLog()")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {
        try {

            // 요청한 사용자의 id
            // SecurityContextHolder를 사용하지 않았기에
            // HttpServletRequest를 직접 호출하여 AuthUserArgumentResolver의 내용물을 활용
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                            .getRequest();

            AuthUser authUser = (AuthUser) request.getAttribute("authUser");
            Long userId = authUser.getId();

            // API 요청 시각
            LocalDateTime requestTime = LocalDateTime.now();

            // API 요청 URL
            String requestURL = String.valueOf(request.getRequestURL());

            // 요청 본문(RequestBody)
            String requestBodyJson = getString(joinPoint);

            // 메소드 실행
            Object result = joinPoint.proceed();

            // 응답 본문(ResponseBody)
            ObjectMapper objectMapper = new ObjectMapper();
            String responseJson = objectMapper.writeValueAsString(result);

            log.info("""
                            AOP
                            사용자 ID : {}
                            API 요청 시각 : {}
                            API 요청 URL : {}
                            요청 본문 : {}
                            응답 본문 : {}
                            """,
                    userId, requestTime, requestURL, requestBodyJson, responseJson
            );

            return result;
        } catch (Exception e) {
            // 예외 발생 시에도 로깅
            log.error("API 실행 중 오류 발생, 오류: {}", e.getMessage());
            throw e;
        }
    }

    private static String getString(ProceedingJoinPoint joinPoint) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Object requestBody = null; // @RequestBody만 추출 (HttpServletRequest, AuthUser 등 제외)
        for (Object arg : joinPoint.getArgs()) {
            if (arg != null &&
                    !(arg instanceof HttpServletRequest) &&
                    !(arg instanceof HttpServletResponse) &&
                    !(arg instanceof AuthUser)) {  // JWT 인증 객체 제외
                requestBody = arg;
                break;
            }
        }

        // JSON 문자열로 변환
        return objectMapper.writeValueAsString(requestBody);
    }
}
