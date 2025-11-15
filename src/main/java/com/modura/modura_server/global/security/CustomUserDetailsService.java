package com.modura.modura_server.global.security;

import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        Long id;
        try {
            id = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userId);
            throw new UsernameNotFoundException("Invalid user ID format.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        if (user.isInactive()) {
            // ROLE_INACTIVE 권한 부여
            return new CustomUserDetails(user, List.of(new SimpleGrantedAuthority("ROLE_INACTIVE")));
        }

        return new CustomUserDetails(user);
    }
}