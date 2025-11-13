package com.modura.modura_server.global.tmdb.runner;

import com.modura.modura_server.domain.content.service.PopularContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job tmdbSeedingJob;
    private final PopularContentService popularContentService;

    /**
     * fixedDelay = 3600000: 직전 작업이 '종료'된 후 1시간 뒤 실행
     */
    @Scheduled(fixedDelay = 3600000) // 1시간
    public void runJobPeriodically() {
        log.info("Running periodic TMDB seeding job...");
        runJob();

        log.info("Running periodic popular content cache refresh...");
        popularContentService.refreshPopularContent();
    }

    private void runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(tmdbSeedingJob, jobParameters);

        } catch (Exception e) {
            log.error("Failed to run TMDB seeding job", e);
        }
    }
}
