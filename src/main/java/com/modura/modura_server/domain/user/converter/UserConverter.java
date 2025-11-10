package com.modura.modura_server.domain.user.converter;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class UserConverter {

    public static SearchResponseDTO.SearchContentListDTO toGetLikedContentListDTO(List<Content> contentList, boolean isLiked){

        List<SearchResponseDTO.SearchContentDTO> contentDTOList = contentList.stream()
                .map(content -> SearchResponseDTO.SearchContentDTO.builder()
                        .id(content.getId())
                        .title(content.getTitleKr())
                        .isLiked(isLiked)
                        .thumbnail(content.getThumbnail())
                        .build())
                .collect(Collectors.toList());

        return SearchResponseDTO.SearchContentListDTO.builder()
                .contentList(contentDTOList)
                .build();
    }
}
