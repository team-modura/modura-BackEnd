package com.modura.modura_server.global.tmdb.runner;

import com.modura.modura_server.global.tmdb.service.TmdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbDataSeeder implements CommandLineRunner {

    private final TmdbService tmdbService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting TMDB data seeding...");

        // 1페이지부터 3페이지까지 가져오기
        for (int page = 1; page <= 3; page++) {
            try {
                tmdbService.fetchAndSaveMovies(page);
                Thread.sleep(500); // 0.5초 대기
            } catch (Exception e) {
                log.error("Failed to fetch page {} from TMDB", page, e);
            }
        }
        log.info("Finished TMDB data seeding.");
    }
}
