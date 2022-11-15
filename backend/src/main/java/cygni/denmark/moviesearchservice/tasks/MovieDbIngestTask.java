package cygni.denmark.moviesearchservice.tasks;


import com.zaxxer.hikari.HikariDataSource;
import cygni.denmark.moviesearchservice.persistence.repositories.ActorDb;
import cygni.denmark.moviesearchservice.persistence.repositories.MovieDb;
import cygni.denmark.moviesearchservice.search.documents.MovieDocument;
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
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovieDbIngestTask extends AbstractIngestTask {
    private final HikariDataSource hikariDataSource;
    private final MovieDocumentRepository movieDocumentRepository;

    private final ModelMapper modelMapper;
    public static final int PG_BATCH_SIZE = 50;

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
                                batchSaveMovies(movieBuffer))
                        .subscribeOn(Schedulers.boundedElastic()))
                .retry(5)
                .reduce(Long::sum);
    }


    public Long batchSaveMovies(List<MovieDb> movieData) {
        String sql =
                "INSERT INTO movies (id, end_year,original_title," +
                        "primary_title,runtime_minutes,start_year,tconst,title_type,version) " +
                        "VALUES (?, ?, ?, ?, ?,?,?,?,?)";
        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            int counter = 0;
            for (MovieDb movie : movieData) {
                statement.clearParameters();
                statement.setObject(1, movie.getId());
                statement.setInt(2, movie.getEndYear());
                statement.setString(3, movie.getOriginalTitle());
                statement.setString(4, movie.getPrimaryTitle());
                statement.setInt(5, movie.getRuntimeMinutes());
                statement.setInt(6, movie.getStartYear());
                statement.setString(7, movie.getTconst());
                statement.setString(8, movie.getTitleType());
                statement.setLong(9, movie.getVersion());
                statement.addBatch();
                if ((counter + 1) % PG_BATCH_SIZE == 0 || (counter + 1) == movieData.size()) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }
    @Value("${cygni.elasticWindow}")
    public Integer elasticWindowSize;
    private Mono<Long> indexMovieDocuments(Flux<MovieDb> moviesFlux) {
        return moviesFlux// Splits flux into multiple windows so elastic doesn't choke.
                .map(movieDb -> modelMapper.map(movieDb, MovieDocument.class))
                .window(elasticWindowSize)
                .flatMap(movieDocumentRepository::saveAll)
                .count();
    }

}
