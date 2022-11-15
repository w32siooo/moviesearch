package cygni.denmark.moviesearchservice.tasks;

import cygni.denmark.moviesearchservice.search.services.ActorSearchService;
import cygni.denmark.moviesearchservice.services.CleanElasticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class IngestDirigentTask {

    private final ActorDbIngestTask actorDbIngestTask;

    private final MovieIngestTask movieIngestTask;

    private final CleanElasticService cleanElasticService;

    private final ActorSearchService actorSearchService;

    private final ActorDocIngestTask actorDocIngestTask;

    @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 1000)
    public void scheduleFixedRateWithInitialDelayTask() {
        log.info("scheduled task started");
        long startTime = Instant.now().toEpochMilli();
        cleanElasticService
                .cleanRepos()
                .then(actorDbIngestTask.run())
                .doOnSuccess(inserted -> log.info("{} Actor elements indexed in postgres ", inserted))
                .block();

        actorDocIngestTask.streamToElasticFromJpaAndBlock();

        long endTime = Instant.now().toEpochMilli() - startTime;
        log.info("scheduled task ended, it took {} seconds", endTime / 1000);

    }
}
