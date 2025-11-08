package com.modura.modura_server.domain.search.service;

import com.modura.modura_server.domain.search.dto.SearchResponseDTO;

import java.util.List;

public interface SearchQueryService {

    List<SearchResponseDTO.SearchContentDTO> searchContent(Long userId, String query);
}
