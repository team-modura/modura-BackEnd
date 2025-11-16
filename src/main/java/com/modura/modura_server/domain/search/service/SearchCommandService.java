package com.modura.modura_server.domain.search.service;

public interface SearchCommandService {

    void incrementSearchKeyword(String query);
    void seedMovie();
    void seedSeries();
}
