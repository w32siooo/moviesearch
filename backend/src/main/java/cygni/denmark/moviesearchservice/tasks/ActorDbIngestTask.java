package cygni.denmark.moviesearchservice.tasks;

import cygni.denmark.moviesearchservice.persistence.repositories.ActorDb;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.util.set.Sets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActorDbIngestTask extends AbstractIngestTask {

    private final ActorBatchSaveTask actorBatchSaveTask;

    @Value("${cygni.actorsTsv}")
    public String NAME_BASICS_TSV_PATH;

    @Value("${cygni.postgresBufferSize}")
    public Integer PG_BUFFER_SIZE;

    public Mono<Long> run() {
        log.info("Data ingestion of actor file started, requested indexing of {} elements",take);

       return indexActorDbs(getLines(NAME_BASICS_TSV_PATH)
                .skip(1)
                .take(take) // Because we are testing, limit the amount of rows.
                .map(s -> s.replaceAll("\\\\N", "0000"))
                .map(s -> s.split("\t"))
                .flatMap(actors -> reactiveUUIDGenerator()
                        .map(uuid -> Tuples.of(actors, uuid)))
                .map(data ->
                        new ActorDb(data.getT2(),
                                0L,
                                new Timestamp(new Date().getTime()),
                                data.getT1()[0],
                                data.getT1()[1],
                                Integer.parseInt(data.getT1()[2]),
                                Integer.parseInt(data.getT1()[3]),
                                Sets.newHashSet(data.getT1()[5].split(",")),
                                Sets.newHashSet(data.getT1()[4].split(",")))
                )
               .buffer(PG_BUFFER_SIZE)
       );

    }



    private Mono<Long> indexActorDbs(Flux<List<ActorDb>> actorsFlux) {
        return actorsFlux
                .flatMap(actorBuffer -> Mono.fromCallable(() ->
                                actorBatchSaveTask.batchSave(actorBuffer))
                        .subscribeOn(Schedulers.boundedElastic()))
                .retry(5)
                .reduce(Long::sum);
    }


}
