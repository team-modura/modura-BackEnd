package com.modura.modura_server.global.tmdb.runner;

import com.modura.modura_server.domain.search.service.PopularContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job tmdbSeedingJob;
    private final PopularContentService popularContentService;
    private final RedissonClient redissonClient;

    private static final String SEED_MOVIE_LOCK_KEY = "lock:seedMovie";

    /**
     * fixedDelay = 3600000: 직전 작업이 '종료'된 후 1시간 뒤 실행
     */
    @Scheduled(fixedDelay = 3600000) // 1시간
    public void runJobPeriodically() {

        RLock lock = redissonClient.getLock(SEED_MOVIE_LOCK_KEY);
        boolean acquired = false;

        try {
            // 0초간 락 획득 시도 (대기 없음), 락 획득 시 60초간 임대
            acquired = lock.tryLock(0, 60, TimeUnit.SECONDS);

            // 락 획득 실패 시
            if (!acquired) {
                log.warn("Periodic TMDB seeding job skipped: Seeding is already in progress.");
                return;
            }

            log.info("Running periodic TMDB seeding job...");
            boolean jobSuccess = runJob();

            if (jobSuccess) {
                log.info("Running periodic popular content cache refresh...");
                popularContentService.refreshPopularContent();
            } else {
                log.warn("Skipping cache refresh due to job failure");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // 스케줄링 작업 중 예외 발생 시
            log.error("Error during scheduled TMDB job execution: {}", e.getMessage(), e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(tmdbSeedingJob, jobParameters);
            return execution.getStatus() == BatchStatus.COMPLETED;

        } catch (Exception e) {
            log.error("Failed to run TMDB seeding job", e);
            return false;
        }
    }
}
