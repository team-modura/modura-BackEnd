package com.modura.modura_server.domain.user.converter;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.place.dto.PlaceResponseDTO;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.entity.UserStillcut;

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

    public static PlaceResponseDTO.GetStillcutListDTO toGetStillcutListDTO(List<Stillcut> stillcutList){

        List<PlaceResponseDTO.GetStillcutDTO> stillcutDTOList = stillcutList.stream()
                .map(stillcut -> {
                    Content content = stillcut.getContent();

                    return PlaceResponseDTO.GetStillcutDTO.builder()
                            .stillcutId(stillcut.getId())
                            .contentId(stillcut.getContent().getId())
                            .title(stillcut.getContent().getTitleKr())
                            .imageUrl(stillcut.getImageUrl())
                            .build();
                })
                .collect(Collectors.toList());

        return PlaceResponseDTO.GetStillcutListDTO.builder()
                .stillcutList(stillcutDTOList)
                .build();
    }

    public static UserResponseDTO.GetMyStillcutListDTO toGetMyStillcutListDTO(List<UserResponseDTO.GetMyStillcutDTO> stillcutDTOList){

        return UserResponseDTO.GetMyStillcutListDTO.builder()
                .stillcutList(stillcutDTOList)
                .build();
    }
}
