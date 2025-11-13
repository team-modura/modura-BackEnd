package com.modura.modura_server.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularContentCacheDTO implements Serializable {

    // 직렬화/역직렬화 시 버전 관리를 위한 serialVersionUID
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String titleKr;
    private String thumbnail;
}
