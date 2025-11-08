package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.content.entity.Category;
import com.modura.modura_server.domain.content.repository.CategoryRepository;
import com.modura.modura_server.domain.user.dto.UserRequestDTO;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.entity.UserCategory;
import com.modura.modura_server.domain.user.repository.UserCategoryRepository;
import com.modura.modura_server.domain.user.repository.UserRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Void updateUser(Long userId, UserRequestDTO.UpdateUserDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        if (user.getAddress() != null) {
            throw new BusinessException(ErrorStatus.ADDRESS_ALREADY_EXISTS);
        }

        user.updateAddress(request.getAddress());

        if (request.getCategoryList() != null && !request.getCategoryList().isEmpty()) {
            List<UserCategory> newUserCategories = request.getCategoryList().stream()
                    .map(categoryId -> {
                        // Category 엔티티 조회
                        Category category = categoryRepository.findById(categoryId.longValue())
                                .orElseThrow(() -> new BusinessException(ErrorStatus.CATEGORY_NOT_FOUND));
                        // UserCategory 생성
                        return UserCategory.builder()
                                .user(user)
                                .category(category)
                                .build();
                    })
                    .collect(Collectors.toList());

            userCategoryRepository.saveAll(newUserCategories);
        }

        return null;
    }
}