package com.modura.modura_server.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modura.modura_server.global.response.ApiResponse;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws ServletException, IOException {
        log.error("Access denied error: {}", accessDeniedException.getMessage());

        // 403 Forbidden 에러를 JSON 형태로 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiResponse<Object> apiResponse = ApiResponse.onFailure(ErrorStatus.FORBIDDEN);
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
