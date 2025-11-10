package com.modura.modura_server.domain.search.converter;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;

import java.util.List;
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

    public static SearchResponseDTO.SearchPlaceDTO toSearchPlaceDTO(Place place, boolean isLiked) {

        return SearchResponseDTO.SearchPlaceDTO.builder()
                .id(place.getId())
                .name(place.getName())
                .isLiked(isLiked)
                .thumbnail(place.getThumbnail())
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
}
