package com.modura.modura_server.domain.search.service;

import com.modura.modura_server.domain.content.dto.PopularContentCacheDTO;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.Platform;
import com.modura.modura_server.domain.content.repository.ContentLikesRepository;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.domain.content.repository.PlatformRepository;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.repository.PlaceLikesRepository;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.place.repository.PlaceReviewRepository;
import com.modura.modura_server.domain.search.converter.SearchConverter;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final PlaceReviewRepository placeReviewRepository;
    private final PopularContentService popularContentService;
    private final PlatformRepository platformRepository;

    private final SearchCommandService searchCommandService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String POPULAR_KEYWORD_KEY = "popular:keywords";

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDTO.SearchContentListDTO searchContent(Long userId, String query) {

        searchCommandService.incrementSearchKeyword(query);

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
    public SearchResponseDTO.SearchPlaceListDTO searchPlace(Long userId, String query) {

        searchCommandService.incrementSearchKeyword(query);

        List<Place> placesByName = placeRepository.searchByNameContaining(query);
        List<Place> placesByContent = findPlacesByContentTitle(query);
        List<Place> combinedPlaces = combinePlaces(placesByName, placesByContent);

        if (combinedPlaces.isEmpty()) {
            return SearchResponseDTO.SearchPlaceListDTO.builder()
                    .placeList(Collections.emptyList())
                    .build();
        }

        List<Long> placeIds = combinedPlaces.stream()
                .map(Place::getId)
                .toList();

        // '좋아요' 누른 콘텐츠 ID 목록을 한 번의 쿼리로 조회
        Set<Long> likedPlaceIds = placeLikesRepository.findIdsByUserIdAndPlaceIds(userId, placeIds);

        return SearchConverter.toSearchPlaceListDTO(combinedPlaces, likedPlaceIds);
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDTO.GetPopularKeywordDTO getPopularKeyword() {

        // 스코어 높은 순으로 0위부터 4위까지 5개 조회
        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(POPULAR_KEYWORD_KEY, 0, 4);
        List<String> keywordList = (keywords != null) ? new ArrayList<>(keywords) : Collections.emptyList();

        return SearchResponseDTO.GetPopularKeywordDTO.builder()
                .keywords(keywordList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDTO.SearchPlaceListDTO getTopPlace(Long userId) {

        Pageable topTen = PageRequest.of(0, 10);
        List<Long> topPlaceIds = placeReviewRepository.findTopPlaceIdsByReviewCount(topTen);

        if (topPlaceIds.isEmpty()) {
            return SearchResponseDTO.SearchPlaceListDTO.builder()
                    .placeList(Collections.emptyList())
                    .build();
        }

        List<Place> places = placeRepository.findAllById(topPlaceIds);

        Map<Long, Place> placeMap = places.stream()
                .collect(Collectors.toMap(Place::getId, p -> p));

        List<Place> orderedPlaces = topPlaceIds.stream()
                .map(placeMap::get)
                .filter(Objects::nonNull)
                .toList();

        Set<Long> likedPlaceIds = placeLikesRepository.findIdsByUserIdAndPlaceIds(userId, topPlaceIds);

        return SearchConverter.toSearchPlaceListDTO(orderedPlaces, likedPlaceIds);
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDTO.GetTopContentListDTO getTopMovie(Long userId) {

        List<PopularContentCacheDTO> cachedContents = popularContentService.getPopularMovie();

        if (cachedContents.isEmpty()) {
            return SearchResponseDTO.GetTopContentListDTO.builder()
                    .contentList(Collections.emptyList())
                    .build();
        }

        List<Long> contentIds = cachedContents.stream()
                .map(PopularContentCacheDTO::getId)
                .collect(Collectors.toList());

        // '좋아요' 누른 콘텐츠 ID 목록을 한 번의 쿼리로 조회
        Set<Long> likedContentIds = contentLikesRepository.findIdsByUserIdAndContentIds(userId, contentIds);

        Map<Long, List<String>> platformsByContentId = platformRepository.findByContent_IdIn(contentIds)
                .stream()
                .collect(Collectors.groupingBy(
                        platform -> platform.getContent().getId(), // Content ID로 그룹화
                        Collectors.mapping(Platform::getName, Collectors.toList()) // 플랫폼 이름만 리스트로
                ));

        return SearchConverter.toGetTopContentListDTOFromCache(cachedContents, likedContentIds, platformsByContentId);
    }

    @Override
    public SearchResponseDTO.GetTopContentListDTO getTopSeries(Long userId) {

        List<PopularContentCacheDTO> cachedContents = popularContentService.getPopularTVs();

        if (cachedContents.isEmpty()) {
            return SearchResponseDTO.GetTopContentListDTO.builder()
                    .contentList(Collections.emptyList())
                    .build();
        }

        List<Long> contentIds = cachedContents.stream()
                .map(PopularContentCacheDTO::getId)
                .collect(Collectors.toList());

        // '좋아요' 누른 콘텐츠 ID 목록을 한 번의 쿼리로 조회
        Set<Long> likedContentIds = contentLikesRepository.findIdsByUserIdAndContentIds(userId, contentIds);

        Map<Long, List<String>> platformsByContentId = platformRepository.findByContent_IdIn(contentIds)
                .stream()
                .collect(Collectors.groupingBy(
                        platform -> platform.getContent().getId(), // Content ID로 그룹화
                        Collectors.mapping(Platform::getName, Collectors.toList()) // 플랫폼 이름만 리스트로
                ));

        return SearchConverter.toGetTopContentListDTOFromCache(cachedContents, likedContentIds, platformsByContentId);
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