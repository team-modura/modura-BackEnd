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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws ServletException, IOException {
        log.error("Unauthorized error: {}", authException.getMessage());

        // 401 Unauthorized 에러를 JSON 형태로 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Object> apiResponse = ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED);
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
