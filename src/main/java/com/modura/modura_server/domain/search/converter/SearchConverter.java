package com.modura.modura_server.domain.search.converter;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;

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
}
