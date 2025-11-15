package com.modura.modura_server.domain.search.service;

import com.modura.modura_server.domain.search.dto.SearchResponseDTO;

public interface SearchQueryService {

    SearchResponseDTO.SearchContentListDTO searchContent(Long userId, String query);
    SearchResponseDTO.SearchPlaceListDTO searchPlace(Long userId, String query);
    SearchResponseDTO.GetPopularKeywordDTO getPopularKeyword();
    SearchResponseDTO.SearchPlaceListDTO getTopPlace(Long userId);
}
