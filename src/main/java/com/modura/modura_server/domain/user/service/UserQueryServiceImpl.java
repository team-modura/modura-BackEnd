package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.domain.search.converter.SearchConverter;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final ContentRepository contentRepository;

    private static final Map<String, Integer> CONTENT_TYPE_MAP = Map.of(
            "series", 1,
            "movie", 2
    );

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDTO.SearchContentListDTO getLikedContent(Long userId, String type) {

        Integer contentType = CONTENT_TYPE_MAP.get(type.toLowerCase());

        if (contentType == null) {
            return SearchResponseDTO.SearchContentListDTO.builder()
                    .contentList(Collections.emptyList())
                    .build();
        }

        List<Content> likedContents = contentRepository.findLikedContentsByUserAndType(userId, contentType);

        return SearchConverter.toSearchContentListDTO(likedContents, true);
    }
}