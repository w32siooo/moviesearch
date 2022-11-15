package cygni.denmark.moviesearchservice.tasks;


import com.zaxxer.hikari.HikariDataSource;
import cygni.denmark.moviesearchservice.persistence.repositories.ActorDb;
import cygni.denmark.moviesearchservice.persistence.repositories.MovieDb;
import cygni.denmark.moviesearchservice.search.documents.MovieDocument;
import cygni.denmark.moviesearchservice.search.models.Movie;
import cygni.denmark.moviesearchservice.search.repositories.MovieDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.util.set.Sets;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovieDbIngestTask extends AbstractIngestTask {

    private final MovieBatchSaveTask movieBatchSaveTask;



    @Value("${cygni.moviesTsv}")
    public String MOVIES_TSV_PATH;

    public Mono<Long> run() {
        log.info("Data ingestion of movie file started");

        return indexMovies(getLines(MOVIES_TSV_PATH)
                .skip(1)
                .take(take) // Because we are testing, limit the amount of rows.
                .map(s -> s.replaceAll("\\\\N", "0000"))
                .map(s -> s.split("\t"))
                .flatMap(movies -> reactiveUUIDGenerator()
                        .map(uuid -> Tuples.of(movies, uuid)))
                .map(movies ->
                        new MovieDb(
                                movies.getT2(),
                                0L,
                                movies.getT1()[0],
                                movies.getT1()[1],
                                movies.getT1()[2],
                                movies.getT1()[3],
                                Integer.parseInt(movies.getT1()[5]),
                                Integer.parseInt(movies.getT1()[6]),
                                Integer.parseInt(movies.getT1()[7]),
                                Sets.newHashSet(movies.getT1()[8].split(",")))
                ).buffer(PG_BUFFER_SIZE));

    }
    private Mono<Long> indexMovies(Flux<List<MovieDb>> moviesFlux) {
        return moviesFlux
                .flatMap(movieBuffer -> Mono.fromCallable(() ->
                                movieBatchSaveTask.batchSave(movieBuffer))
                        .subscribeOn(Schedulers.boundedElastic()))
                .retry(5)
                .reduce(Long::sum);
    }






}
