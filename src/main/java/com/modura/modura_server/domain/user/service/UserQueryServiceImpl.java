package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.user.converter.UserConverter;
import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.entity.Terms;
import com.modura.modura_server.domain.user.repository.TermsRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final TermsRepository termsRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO.GetTermsDTO getTerms(Long termsId) {

        Terms terms = termsRepository.findById(termsId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.TERMS_NOT_EXIST));

        return UserConverter.toGetTermsDTO(terms);
    }
}