package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import com.modura.modura_server.domain.user.converter.UserConverter;
import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.entity.UserStillcut;
import com.modura.modura_server.domain.user.repository.UserStillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import com.modura.modura_server.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final ContentRepository contentRepository;
    private final PlaceRepository placeRepository;
    private final UserStillcutRepository userStillcutRepository;
    private final S3Service s3Service;

    private static final Map<String, Integer> CONTENT_TYPE_MAP = Map.of(
            "series", 1,
            "movie", 2
    );

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDTO.SearchContentListDTO getLikedContent(Long userId, String type) {

        Integer contentType = CONTENT_TYPE_MAP.get(type.toLowerCase());

        if (contentType == null) {
            return SearchResponseDTO.SearchContentListDTO.builder()
                    .contentList(Collections.emptyList())
                    .build();
        }

        List<Content> likedContents = contentRepository.findLikedContentsByUserAndType(userId, contentType);

        return UserConverter.toGetLikedContentListDTO(likedContents);
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDTO.SearchPlaceListDTO getLikedPlace(Long userId) {

        List<Place> likedPlaces = placeRepository.findLikedPlacesByUser(userId);

        return UserConverter.toGetLikedPlaceListDTO(likedPlaces);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO.GetMyStillcutListDTO getMyStillcutList(Long userId) {

        List<UserStillcut> stillcutList = userStillcutRepository.findAllWithDetailsByUserId(userId);

        List<UserResponseDTO.GetMyStillcutDTO> stillcutDTOList = stillcutList.stream()
                .map(userStillcut -> {
                    String s3Key = userStillcut.getImageUrl();
                    String presignedUrl = s3Service.generateViewPresignedUrl(s3Key);

                    return UserResponseDTO.GetMyStillcutDTO.builder()
                            .id(userStillcut.getId())
                            .imageUrl(presignedUrl)
                            .build();
                })
                .collect(Collectors.toList());

        return UserConverter.toGetMyStillcutListDTO(stillcutDTOList);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO.GetMyStillcutDetailDTO getMyStillcutDetail(Long userId, Long stillcutId) {

        UserStillcut userStillcut = userStillcutRepository.findUserDetailsById(userId, stillcutId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.USER_STILLCUT_NOT_FOUND));

        String s3Key = userStillcut.getImageUrl();
        String presignedUrl = s3Service.generateViewPresignedUrl(s3Key);

        Stillcut stillcut = userStillcut.getStillcut();
        Content content = stillcut.getContent();
        Place place = stillcut.getPlace();

        return UserResponseDTO.GetMyStillcutDetailDTO.builder()
                .id(userStillcut.getId())
                .imageUrl(presignedUrl)
                .stillcut(stillcut.getImageUrl())
                .title(content.getTitleKr())
                .name(place.getName())
                .date(userStillcut.getCreatedAt().toLocalDate().toString())
                .build();
    }
}