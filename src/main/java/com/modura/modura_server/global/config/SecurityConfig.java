package com.modura.modura_server.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modura.modura_server.global.jwt.JwtAuthenticationFilter;
import com.modura.modura_server.global.jwt.JwtProvider;
import com.modura.modura_server.global.security.JwtAccessDeniedHandler;
import com.modura.modura_server.global.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(handler ->
                        handler.authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401
                                .accessDeniedHandler(jwtAccessDeniedHandler)) // 403

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/signup", "/auth/login", "/auth/token").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers("/users/**", "/contents/**", "/places/**", "/search/**", "/s3/**").hasRole("USER")
                        .requestMatchers("/auth/reactivate").hasRole("INACTIVE")
                        .requestMatchers("/auth/reissue", "/auth/logout").hasAnyRole("USER", "INACTIVE")

                        .anyRequest().authenticated())

                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, redisTemplate, objectMapper),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
