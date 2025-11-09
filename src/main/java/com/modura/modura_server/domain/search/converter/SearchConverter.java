package com.modura.modura_server.domain.search.converter;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;

public class SearchConverter {

    public static SearchResponseDTO.SearchContentDTO toSearchContentDTO(Content content, boolean isLiked) {

        return SearchResponseDTO.SearchContentDTO.builder()
                .id(content.getId())
                .title(content.getTitleKr())
                .thumbnail(content.getThumbnail())
                .isLiked(isLiked)
                .build();
    }
}
