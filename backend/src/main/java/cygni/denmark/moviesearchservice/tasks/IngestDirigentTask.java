package cygni.denmark.moviesearchservice.tasks;

import cygni.denmark.moviesearchservice.persistence.repositories.ActorRepository;
import cygni.denmark.moviesearchservice.persistence.repositories.MovieRepository;
import cygni.denmark.moviesearchservice.persistence.repositories.StateDb;
import cygni.denmark.moviesearchservice.persistence.repositories.StateValue;
import cygni.denmark.moviesearchservice.search.services.ActorSearchService;
import cygni.denmark.moviesearchservice.search.services.MovieSearchService;
import cygni.denmark.moviesearchservice.services.CleanElasticService;
import cygni.denmark.moviesearchservice.services.StateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cygni", name = "ingest", havingValue = "true")
public class IngestDirigentTask {

  private final ActorDbIngestTask actorDbIngestTask;

  private final MovieDbIngestTask movieDbIngestTask;

  private final MovieDocIngestTask movieDocIngestTask;

  private final ActorDocIngestTask actorDocIngestTask;

  private final StateService stateService;

  @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 1000)
  public void scheduleFixedRateWithInitialDelayTask() {
    log.info("scheduled task started");

    var hot = stateService.getState().cache();

    var ingestActors =
        hot.filter(state -> state == StateValue.INGESTING)
            .flatMap(state -> actorDbIngestTask.run())
            .doOnSuccess(
                (state) -> {
                  log.info("actorDbIngestTask finished indexing {} actors", state);
                })
            .map(s-> stateService.updateState(StateValue.ACTORS_INGESTED_PG));

    var ingestMovies =
        hot.filter(state -> state == StateValue.ACTORS_INGESTED_PG)
            .flatMap(state -> movieDbIngestTask.run())
            .doOnSuccess(
                (state) -> {
                  log.info("movieDbIngestTask finished indexing {} movies", state);
                })
            .map(s->stateService.updateState(StateValue.MOVIES_INGESTED_PG));

    var ingestMovieDocs =
        hot
            .filter(state -> state == StateValue.MOVIES_INGESTED_PG)
            .flatMap(
                state -> movieDocIngestTask.streamToElasticFromJpaAndBlock())
            .doOnSuccess(
                (state) -> {
                  log.info("movieDocIngestTask finished indexing {} movies", state);
                })
            .map(s-> stateService.updateState(StateValue.MOVIES_INGESTED_ES));

    var ingestActorDocs =
        hot.filter(state -> state == StateValue.MOVIES_INGESTED_ES)
            .flatMap(
                state ->
                    actorDocIngestTask.streamToElasticFromJpaAndBlock())
            .doOnSuccess(
                (state) -> {
                  log.info("actorDocIngestTask finished indexing {} actors", state);
                })
            .map(s-> stateService.updateState(StateValue.FINISHED));

    Mono.zip(ingestActors, ingestMovies, ingestMovieDocs, ingestActorDocs)
        .subscribe(
            (state) -> {
              log.info("ingest finished");
            });
    long startTime = Instant.now().toEpochMilli();

    long endTime = Instant.now().toEpochMilli() - startTime;
    log.info(
        "scheduled task ended, it took {} seconds and {} miliseconds",
        endTime / 1000,
        endTime % 1000);
  }
}
