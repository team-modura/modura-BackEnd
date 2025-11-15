package com.modura.modura_server.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.ApiResponse;
import com.modura.modura_server.global.response.code.BaseCode;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Request Header에서 토큰 추출
            String token = jwtProvider.resolveToken(request);

            // 2. 토큰 유효성 검사
            if (token == null || !jwtProvider.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (isTokenBlacklisted(token)) {
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, ErrorStatus.INVALID_TOKEN);
                return;
            }

            // 3. 토큰이 유효하고 블랙리스트에 없을 경우, 토큰에서 Authentication 객체를 가져와 SecurityContext에 저장
            setAuthentication(token);

            // 4. 다음 필터로 진행
            filterChain.doFilter(request, response);
        } catch (BusinessException ex) {
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, ex.getBaseCode());
        }
    }

    private boolean isTokenBlacklisted(String token) {

        String isBlacklisted = redisTemplate.opsForValue().get(token);
        // "logout" 문자열이 있는지 확인
        return StringUtils.hasText(isBlacklisted);
    }

    private void setAuthentication(String token) {

        try {
            Authentication authentication = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameNotFoundException ex) {
            // 토큰은 유효하나, 토큰 발급 이후 사용자가 DB에서 삭제된 경우 등
            SecurityContextHolder.clearContext();
        }
    }

    private void sendErrorResponse(HttpServletResponse response, BaseCode baseCode) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(baseCode.getHttpStatus().value());

        ApiResponse<Object> apiResponse = ApiResponse.onFailure(baseCode);
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
