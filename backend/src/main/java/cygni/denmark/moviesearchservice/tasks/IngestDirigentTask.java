package cygni.denmark.moviesearchservice.tasks;

import cygni.denmark.moviesearchservice.persistence.repositories.ActorRepository;
import cygni.denmark.moviesearchservice.persistence.repositories.MovieRepository;
import cygni.denmark.moviesearchservice.search.services.ActorSearchService;
import cygni.denmark.moviesearchservice.search.services.MovieSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cygni", name = "ingest", havingValue = "true")
public class IngestDirigentTask {

  private final ActorDbIngestTask actorDbIngestTask;

  private final MovieDbIngestTask movieDbIngestTask;

  private final MovieSearchService movieSearchService;

  private final ActorSearchService actorSearchService;

  private final ActorRepository actorRepository;

  private final MovieRepository movieRepository;

  private final MovieDocIngestTask movieDocIngestTask;

  private final ActorDocIngestTask actorDocIngestTask;

  @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 1000)
  public void scheduleFixedRateWithInitialDelayTask() {
    log.info("scheduled task started");
    long startTime = Instant.now().toEpochMilli();
    if (actorRepository.count() < movieDbIngestTask.take) {
      actorDbIngestTask
          .run()
          .doOnSuccess(inserted -> log.info("{} Actor elements indexed in postgres ", inserted))
          .block();
    }

    if (movieRepository.count() < movieDbIngestTask.take) {
      movieDbIngestTask
          .run()
          .doOnSuccess(inserted -> log.info("{} Movie elements indexed in postgres ", inserted))
          .block();
    }

    if (actorSearchService.count().block() < movieDbIngestTask.take) {
      actorDocIngestTask.streamToElasticFromJpaAndBlock();
    }
    if (movieSearchService.count().block() < movieDbIngestTask.take) {
      movieDocIngestTask.streamToElasticFromJpaAndBlock();
    }
    long endTime = Instant.now().toEpochMilli() - startTime;
    log.info(
        "scheduled task ended, it took {} seconds and {} miliseconds",
        endTime / 1000,
        endTime % 1000);
  }
}
