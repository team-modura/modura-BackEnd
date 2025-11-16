package com.modura.modura_server.global.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TmdbProviderResponseDTO {

    private Results results;

    @Getter
    @NoArgsConstructor
    public static class Results {

        @JsonProperty("KR")
        private ProviderCountryDetails KR;
    }

    @Getter
    @NoArgsConstructor
    public static class ProviderCountryDetails {

        private List<ProviderInfo> flatrate;
    }

    @Getter
    @NoArgsConstructor
    public static class ProviderInfo {

        @JsonProperty("provider_name")
        private String providerName;
    }
}
