package cygni.denmark.moviesearchservice.tasks;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import cygni.denmark.moviesearchservice.persistence.repositories.ActorRepository;
import cygni.denmark.moviesearchservice.search.services.ActorSearchService;
import cygni.denmark.moviesearchservice.search.services.MovieSearchService;
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

  private final MovieDbIngestTask movieDbIngestTask;

  private final MovieSearchService movieSearchService;

  private final ActorSearchService actorSearchService;

  private final ActorRepository actorRepository;

  private final MovieDocIngestTask movieDocIngestTask;

  private final ActorDocIngestTask actorDocIngestTask;

  @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 1000)
  public void scheduleFixedRateWithInitialDelayTask() {
    log.info("scheduled task started");
    long startTime = Instant.now().toEpochMilli();
    if (actorRepository.count() == 0) {
      actorDbIngestTask
          .run()
          .doOnSuccess(inserted -> log.info("{} Actor elements indexed in postgres ", inserted))
          .then(movieDbIngestTask.run())
          .doOnSuccess(inserted -> log.info("{} Movie elements indexed in postgres ", inserted))
          .block();
    }
    if (actorSearchService.count().block() == 0) actorDocIngestTask.streamToElasticFromJpaAndBlock();

    if (movieSearchService.count().block() == 0) movieDocIngestTask.streamToElasticFromJpaAndBlock();

    long endTime = Instant.now().toEpochMilli() - startTime;
    log.info(
        "scheduled task ended, it took {} seconds and {} miliseconds",
        endTime / 1000,
        endTime % 1000);
  }
}
