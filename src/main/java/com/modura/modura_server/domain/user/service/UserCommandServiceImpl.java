package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.content.entity.Category;
import com.modura.modura_server.domain.content.repository.CategoryRepository;
import com.modura.modura_server.domain.user.dto.UserRequestDTO;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.entity.UserCategory;
import com.modura.modura_server.domain.user.entity.UserStillcut;
import com.modura.modura_server.domain.user.repository.UserCategoryRepository;
import com.modura.modura_server.domain.user.repository.UserRepository;
import com.modura.modura_server.domain.user.repository.UserStillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import com.modura.modura_server.global.s3.S3Service;
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
    private final UserStillcutRepository userStillcutRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public void updateUser(Long userId, UserRequestDTO.UpdateUserDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        if (user.getAddress() != null) {
            throw new BusinessException(ErrorStatus.ADDRESS_ALREADY_EXISTS);
        }

        user.updateAddress(request.getAddress());

        if (request.getCategoryList() != null && !request.getCategoryList().isEmpty()) {
            userCategoryRepository.deleteByUser(user);

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
    }

    @Override
    @Transactional
    public void delMyStillcut(Long userId, Long stillcutId) {

        UserStillcut userStillcut = userStillcutRepository.findUserDetailsById(userId, stillcutId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.USER_STILLCUT_NOT_FOUND));

        // S3에서 원본 이미지 파일 삭제
        String imageKey = userStillcut.getImageUrl();
        s3Service.deleteFile(imageKey);

        userStillcutRepository.delete(userStillcut);
    }
}