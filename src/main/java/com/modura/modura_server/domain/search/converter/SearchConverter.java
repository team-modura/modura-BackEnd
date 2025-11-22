package com.modura.modura_server.domain.search.converter;

import com.modura.modura_server.domain.content.dto.PopularContentCacheDTO;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchConverter {

    public static SearchResponseDTO.SearchContentDTO toSearchContentDTO(Content content, boolean isLiked) {

        return SearchResponseDTO.SearchContentDTO.builder()
                .id(content.getId())
                .title(content.getTitleKr())
                .isLiked(isLiked)
                .thumbnail(content.getThumbnail())
                .build();
    }

    public static SearchResponseDTO.SearchPlaceDTO toSearchPlaceDTO(Place place, boolean isLiked, String thumbnailUrl) {

        return SearchResponseDTO.SearchPlaceDTO.builder()
                .id(place.getId())
                .name(place.getName())
                .isLiked(isLiked)
                .thumbnail(thumbnailUrl)
                .build();
    }

    public static SearchResponseDTO.SearchContentListDTO toSearchContentListDTO(List<Content> contentList, Set<Long> likedContentIds){

        List<SearchResponseDTO.SearchContentDTO> contentDTOList = contentList.stream()
                .map(content -> {
                    boolean isLiked = likedContentIds.contains(content.getId());
                    return toSearchContentDTO(content, isLiked);
                })
                .collect(Collectors.toList());

        return SearchResponseDTO.SearchContentListDTO.builder()
                .contentList(contentDTOList)
                .build();
    }

    public static SearchResponseDTO.SearchPlaceListDTO toSearchPlaceListDTO(List<SearchResponseDTO.SearchPlaceDTO> placeList){

        return SearchResponseDTO.SearchPlaceListDTO.builder()
                .placeList(placeList)
                .build();
    }

    public static SearchResponseDTO.GetTopContentListDTO toGetTopContentListDTOFromCache(List<PopularContentCacheDTO> contentList,
                                                                                         Set<Long> likedContentIds,
                                                                                         Map<Long, List<String>> platformsByContentId){

        List<SearchResponseDTO.GetTopContentDTO> contentDTOList = contentList.stream()
                .map(cacheDto -> {
                    boolean isLiked = likedContentIds.contains(cacheDto.getId());
                    List<String> platforms = platformsByContentId.getOrDefault(cacheDto.getId(), Collections.emptyList());
                    return toGetTopContentDTOFromCache(cacheDto, isLiked, platforms);
                })
                .collect(Collectors.toList());

        return SearchResponseDTO.GetTopContentListDTO.builder()
                .contentList(contentDTOList)
                .build();
    }

    public static SearchResponseDTO.GetTopContentDTO toGetTopContentDTOFromCache(PopularContentCacheDTO cacheDTO,
                                                                                 boolean isLiked,
                                                                                 List<String> platforms) {

        return SearchResponseDTO.GetTopContentDTO.builder()
                .id(cacheDTO.getId())
                .title(cacheDTO.getTitleKr())
                .isLiked(isLiked)
                .thumbnail(cacheDTO.getThumbnail())
                .platforms(platforms)
                .build();
    }
}
