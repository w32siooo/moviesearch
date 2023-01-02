package cygni.denmark.moviesearchservice.tasks;

import cygni.denmark.moviesearchservice.persistence.repositories.StateValue;
import cygni.denmark.moviesearchservice.services.StateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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

    var hot =
        stateService
            .getState()
            .flatMap(
                stateValue -> {
                  if (stateValue == StateValue.INGESTING) {
                    return actorDbIngestTask
                        .run()
                        .flatMap(s -> stateService.updateState(StateValue.ACTORS_INGESTED_PG));
                  }
                  if (stateValue == StateValue.ACTORS_INGESTED_PG) {
                    return movieDbIngestTask
                        .run()
                        .flatMap(s -> stateService.updateState(StateValue.MOVIES_INGESTED_PG));
                  }
                  if (stateValue == StateValue.MOVIES_INGESTED_PG) {
                    return actorDocIngestTask
                        .run()
                        .flatMap(s -> stateService.updateState(StateValue.ACTORS_INGESTED_ES));
                  }
                  if (stateValue == StateValue.ACTORS_INGESTED_ES) {
                    return movieDocIngestTask
                        .run()
                        .flatMap(s -> stateService.updateState(StateValue.FINISHED));
                  } else {
                    return Mono.empty();
                  }
                })
            .subscribe(s -> log.info("ingested {} finished",s));

    long startTime = Instant.now().toEpochMilli();

    long endTime = Instant.now().toEpochMilli() - startTime;
    log.info(
        "scheduled task ended, it took {} seconds and {} miliseconds",
        endTime / 1000,
        endTime % 1000);
  }
}
