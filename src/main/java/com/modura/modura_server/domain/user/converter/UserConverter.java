package com.modura.modura_server.domain.user.converter;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class UserConverter {

    public static SearchResponseDTO.SearchContentListDTO toGetLikedContentListDTO(List<Content> contentList){

        List<SearchResponseDTO.SearchContentDTO> contentDTOList = contentList.stream()
                .map(content -> SearchResponseDTO.SearchContentDTO.builder()
                        .id(content.getId())
                        .title(content.getTitleKr())
                        .isLiked(true)
                        .thumbnail(content.getThumbnail())
                        .build())
                .collect(Collectors.toList());

        return SearchResponseDTO.SearchContentListDTO.builder()
                .contentList(contentDTOList)
                .build();
    }

    public static SearchResponseDTO.SearchPlaceListDTO toGetLikedPlaceListDTO(List<Place> placeList){

        List<SearchResponseDTO.SearchPlaceDTO> placeDTOList = placeList.stream()
                .map(place -> SearchResponseDTO.SearchPlaceDTO.builder()
                        .id(place.getId())
                        .name(place.getName())
                        .isLiked(true)
                        .thumbnail(place.getThumbnail())
                        .build())
                .collect(Collectors.toList());

        return SearchResponseDTO.SearchPlaceListDTO.builder()
                .placeList(placeDTOList)
                .build();
    }
}
