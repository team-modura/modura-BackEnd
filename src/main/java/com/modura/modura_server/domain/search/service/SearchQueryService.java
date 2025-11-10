package com.modura.modura_server.domain.search.service;

import com.modura.modura_server.domain.search.dto.SearchResponseDTO;

import java.util.List;

public interface SearchQueryService {

    SearchResponseDTO.SearchContentListDTO searchContent(Long userId, String query);
    List<SearchResponseDTO.SearchPlaceDTO> searchPlace(Long userId, String query);
}
