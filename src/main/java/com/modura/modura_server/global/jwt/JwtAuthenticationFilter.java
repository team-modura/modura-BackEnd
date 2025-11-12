package com.modura.modura_server.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Request Header에서 토큰 추출
        String token = jwtProvider.resolveToken(request);

        // 2. 토큰 유효성 검사
        if (token != null && jwtProvider.validateToken(token)) {
            Object isBlacklisted = redisTemplate.opsForValue().get(token);

            if (StringUtils.hasText((String)isBlacklisted)) {
                // 로그아웃 처리된 토큰일 경우, 컨텍스트를 비우고 다음 필터로 진행
                SecurityContextHolder.clearContext();
            } else {
                // 3. 토큰이 유효하고 블랙리스트에 없을 경우, 토큰에서 Authentication 객체를 가져와 SecurityContext에 저장
                try {
                    Authentication authentication = jwtProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (UsernameNotFoundException ex) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        // 4. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}
