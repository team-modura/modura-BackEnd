package com.modura.modura_server.global.tmdb.runner;

import com.modura.modura_server.domain.search.service.PopularContentService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TmdbJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job tmdbMovieSeedingJob;
    private final Job tmdbTVSeedingJob;
    private final PopularContentService popularContentService;
    private final RedissonClient redissonClient;

    private static final String SEED_MOVIE_LOCK_KEY = "lock:seedMovie";
    private static final String SEED_TV_LOCK_KEY = "lock:seedTv";

    public TmdbJobScheduler(JobLauncher jobLauncher,
                            @Qualifier("tmdbMovieSeedingJob") Job tmdbMovieSeedingJob,
                            @Qualifier("tmdbTVSeedingJob") Job tmdbTVSeedingJob,
                            PopularContentService popularContentService,
                            RedissonClient redissonClient) {
        this.jobLauncher = jobLauncher;
        this.tmdbMovieSeedingJob = tmdbMovieSeedingJob;
        this.tmdbTVSeedingJob = tmdbTVSeedingJob;
        this.popularContentService = popularContentService;
        this.redissonClient = redissonClient;
    }

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void runJobPeriodically() {

        // 1. 영화 작업 실행
        boolean movieJobSuccess = runMovieJob();

        // 2. TV 작업 실행
        boolean tvJobSuccess = runTvJob();

        // 3. 캐시 갱신 (두 작업 중 하나라도 성공했다면 갱신)
        if (movieJobSuccess || tvJobSuccess) {
            log.info("Running periodic popular content cache refresh...");
            popularContentService.refreshPopularMovies();
            popularContentService.refreshPopularTVs();
        } else {
            log.warn("Skipping cache refresh as all seeding jobs failed or were skipped.");
        }
    }

    private boolean runMovieJob() {

        RLock lock = redissonClient.getLock(SEED_MOVIE_LOCK_KEY);
        boolean acquired = false;
        try {
            // 0초간 락 획득 시도 (대기 없음), 락 획득 시 3분간 임대
            acquired = lock.tryLock(0, 180, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("Periodic TMDB Movie seeding job skipped: Seeding is already in progress.");
                return false;
            }
            log.info("Running periodic TMDB Movie seeding job...");

            return runJob(tmdbMovieSeedingJob, "MovieJob");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Movie seeding job interrupted.", e);
            return false;
        } catch (Exception e) {
            log.error("Error during scheduled TMDB Movie job execution: {}", e.getMessage(), e);
            return false;
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean runTvJob() {

        RLock lock = redissonClient.getLock(SEED_TV_LOCK_KEY);
        boolean acquired = false;
        try {
            // 0초간 락 획득 시도 (대기 없음), 락 획득 시 3분간 임대
            acquired = lock.tryLock(0, 180, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("Periodic TMDB TV seeding job skipped: Seeding is already in progress.");
                return false;
            }
            log.info("Running periodic TMDB TV seeding job...");

            return runJob(tmdbTVSeedingJob, "TVJob");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("TV seeding job interrupted.", e);
            return false;
        } catch (Exception e) {
            log.error("Error during scheduled TMDB TV job execution: {}", e.getMessage(), e);
            return false;
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean runJob(Job job, String jobName) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);
            return execution.getStatus() == BatchStatus.COMPLETED;

        } catch (Exception e) {
            log.error("Failed to run TMDB seeding job: {}", jobName, e);
            return false;
        }
    }
}
