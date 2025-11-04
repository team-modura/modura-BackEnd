package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.user.converter.AuthConverter;
import com.modura.modura_server.domain.user.dto.UserRequestDTO;
import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.entity.enums.Role;
import com.modura.modura_server.domain.user.repository.UserRepository;
import com.modura.modura_server.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserResponseDTO.GetUserDTO createUser(UserRequestDTO.CreateUserDTO request) {

        User user = User.builder()
                .nickname(request.getNickname())
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return AuthConverter.toUser(user, accessToken, refreshToken);
    }
}