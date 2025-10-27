package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.user.dto.UserResponseDTO;

public interface UserQueryService {

    UserResponseDTO.GetTermsDTO getTerms(Long termsId);
}
