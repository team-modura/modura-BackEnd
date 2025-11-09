package com.modura.modura_server.domain.search.service;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.ContentLikesRepository;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.repository.PlaceLikesRepository;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.search.converter.SearchConverter;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchQueryServiceImpl implements SearchQueryService {

    private final ContentRepository contentRepository;
    private final ContentLikesRepository contentLikesRepository;
    private final PlaceRepository placeRepository;
    private final PlaceLikesRepository placeLikesRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SearchResponseDTO.SearchContentDTO> searchContent(Long userId, String query) {

        List<Content> contents = contentRepository.searchByTitleContaining(query);

        if (contents.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> contentIds = contents.stream()
                .map(Content::getId)
                .toList();

        // '좋아요' 누른 콘텐츠 ID 목록을 한 번의 쿼리로 조회
        Set<Long> likedContentIds = contentLikesRepository.findIdsByUserIdAndContentIds(userId, contentIds);

        return contents.stream()
                .map(content -> {
                    boolean isLiked = likedContentIds.contains(content.getId());
                    return SearchConverter.toSearchContentDTO(content, isLiked);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchResponseDTO.SearchPlaceDTO> searchPlace(Long userId, String query) {

        List<Place> places = placeRepository.searchByNameContaining(query);

        if (places.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> placeIds = places.stream()
                .map(Place::getId)
                .toList();

        // '좋아요' 누른 콘텐츠 ID 목록을 한 번의 쿼리로 조회
        Set<Long> likedPlaceIds = placeLikesRepository.findIdsByUserIdAndPlaceIds(userId, placeIds);

        return places.stream()
                .map(place -> {
                    boolean isLiked = likedPlaceIds.contains(place.getId());
                    return SearchConverter.toSearchPlaceDTO(place, isLiked);
                })
                .collect(Collectors.toList());
    }
}