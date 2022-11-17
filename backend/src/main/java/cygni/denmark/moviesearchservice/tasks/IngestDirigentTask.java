package cygni.denmark.moviesearchservice.tasks;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
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

  private final MovieDbIngestTask movieDbIngestTask;

  private final CleanElasticService cleanElasticService;

  private final ActorSearchService actorSearchService;

  private final HikariDataSource hikariDataSource;

  private final MovieDocIngestTask movieDocIngestTask;

  private final ActorDocIngestTask actorDocIngestTask;

  private boolean ingestToElastic = false;

  @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 1000)
  public void scheduleFixedRateWithInitialDelayTask() {
    log.info("scheduled task started");
    long startTime = Instant.now().toEpochMilli();
    cleanElasticService
        .cleanRepos()
       // .then(actorDbIngestTask.run())
        .doOnSuccess(inserted -> log.info("{} Actor elements indexed in postgres ", inserted))
       // .then(movieDbIngestTask.run())
        .doOnSuccess(inserted -> log.info("{} Movie elements indexed in postgres ", inserted))
        .block();
    actorDocIngestTask.streamToElasticFromJpaAndBlock(1000000, "2022-11-16 15:22:34.093");

    if (ingestToElastic) {
      var res =
          actorDocIngestTask.streamToElasticFromJpaAndBlock(100000, "2022-11-16 15:22:34.093");
      for (int i = 0; i < 8; i++) {
        log.info("progress: "+"=".repeat(i));
        var newRes = actorDocIngestTask.streamToElasticFromJpaAndBlock(100000, res.toString());
        if (newRes == null) {
          actorDocIngestTask.streamToElasticFromJpaAndBlock(100000, res.toString());
        } else {
          res = newRes;
        }
      }
      log.info("actor elements also indexed in elastic" + res);

     // movieDocIngestTask.streamToElasticFromJpaAndBlock();
    }
    long endTime = Instant.now().toEpochMilli() - startTime;
    log.info(
        "scheduled task ended, it took {} seconds and {} miliseconds",
        endTime / 1000,
        endTime % 1000);
  }
}
