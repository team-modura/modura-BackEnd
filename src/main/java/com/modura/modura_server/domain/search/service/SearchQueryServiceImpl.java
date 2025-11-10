package com.modura.modura_server.domain.search.service;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.ContentLikesRepository;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.repository.PlaceLikesRepository;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.search.converter.SearchConverter;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchQueryServiceImpl implements SearchQueryService {

    private final ContentRepository contentRepository;
    private final ContentLikesRepository contentLikesRepository;
    private final PlaceRepository placeRepository;
    private final PlaceLikesRepository placeLikesRepository;
    private final StillcutRepository stillcutRepository;

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDTO.SearchContentListDTO searchContent(Long userId, String query) {

        List<Content> contents = contentRepository.searchByTitleContaining(query);

        if (contents.isEmpty()) {
            return SearchResponseDTO.SearchContentListDTO.builder()
                    .contentList(Collections.emptyList())
                    .build();
        }

        List<Long> contentIds = contents.stream()
                .map(Content::getId)
                .toList();

        // '좋아요' 누른 콘텐츠 ID 목록을 한 번의 쿼리로 조회
        Set<Long> likedContentIds = contentLikesRepository.findIdsByUserIdAndContentIds(userId, contentIds);

        return SearchConverter.toSearchContentListDTO(contents, likedContentIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchResponseDTO.SearchPlaceDTO> searchPlace(Long userId, String query) {

        List<Place> placesByName = placeRepository.searchByNameContaining(query);
        List<Place> placesByContent = findPlacesByContentTitle(query);
        List<Place> combinedPlaces = combinePlaces(placesByName, placesByContent);

        if (combinedPlaces.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> placeIds = combinedPlaces.stream()
                .map(Place::getId)
                .toList();

        // '좋아요' 누른 콘텐츠 ID 목록을 한 번의 쿼리로 조회
        Set<Long> likedPlaceIds = placeLikesRepository.findIdsByUserIdAndPlaceIds(userId, placeIds);

        return combinedPlaces.stream()
                .map(place -> {
                    boolean isLiked = likedPlaceIds.contains(place.getId());
                    return SearchConverter.toSearchPlaceDTO(place, isLiked);
                })
                .collect(Collectors.toList());
    }

    private List<Place> findPlacesByContentTitle(String query) {

        List<Content> contents = contentRepository.searchByTitleContaining(query);
        if (contents.isEmpty()) {
            return Collections.emptyList();
        }

        List<Stillcut> stillcuts = stillcutRepository.findWithPlaceByContentIn(contents);

        // Stillcut에서 Place 추출 (중복 제거)
        return stillcuts.stream()
                .map(Stillcut::getPlace) // LAZY 로딩이 아닌, 이미 Fetch된 Place 객체입니다.
                .distinct()
                .toList();
    }

    private List<Place> combinePlaces(List<Place> placesByName, List<Place> placesByContent) {
        // Map을 사용하여 ID 기준 중복 제거
        Map<Long, Place> combinedPlaceMap = new LinkedHashMap<>();

        placesByName.forEach(place -> combinedPlaceMap.put(place.getId(), place));
        placesByContent.forEach(place -> combinedPlaceMap.putIfAbsent(place.getId(), place));

        return new ArrayList<>(combinedPlaceMap.values());
    }
}