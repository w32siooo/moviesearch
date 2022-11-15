package cygni.denmark.moviesearchservice.tasks;


import com.zaxxer.hikari.HikariDataSource;
import cygni.denmark.moviesearchservice.persistence.repositories.ActorDb;
import cygni.denmark.moviesearchservice.persistence.repositories.MovieDb;
import cygni.denmark.moviesearchservice.search.documents.MovieDocument;
import cygni.denmark.moviesearchservice.search.repositories.MovieDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovieIngestTask extends AbstractIngestTask {
    private final HikariDataSource hikariDataSource;
    private final MovieDocumentRepository movieDocumentRepository;

    private final ModelMapper modelMapper;
    public static final int PG_BATCH_SIZE = 50;

    @Value("${cygni.moviesTsv}")
    public String MOVIES_TSV_PATH;

    public Mono<Long> run(long batchSize) {
        log.info("Data ingestion of movie file started");

        Flux<MovieDb> moviesFlux = getLines(MOVIES_TSV_PATH)
                .skip(1)
                .take(batchSize) // Because we are testing, limit the amount of rows.
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
                                !Objects.equals(movies.getT1()[4], "0"),
                                Integer.parseInt(movies.getT1()[5]),
                                Integer.parseInt(movies.getT1()[6]),
                                Integer.parseInt(movies.getT1()[7]),
                                Arrays.asList(movies.getT1()[8].split(",")))
                )
                .publish()
                .autoConnect(2);


        return Mono.zip(indexMovies(moviesFlux), indexMovieDocuments(moviesFlux))
                .map(Tuple2::getT2);
    }

    private Mono<Long> indexMovies(Flux<MovieDb> moviesFlux) {
        return moviesFlux.count();
    }

    public void batchSaveActors(List<ActorDb> actorData) {
        String sql =
                "INSERT INTO actors (id, version, nconst, birth_year, death_year, primary_name) " +
                        "VALUES (?, ?, ?, ?, ?,?)";
        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            int counter = 0;
            for (ActorDb actor : actorData) {
                statement.clearParameters();
                statement.setObject(1, actor.getId());
                statement.setLong(2, actor.getVersion());
                statement.setString(3, actor.getNconst());
                statement.setInt(4, actor.getBirthYear());
                statement.setInt(5, actor.getDeathYear());
                statement.setString(6, actor.getPrimaryName());
                statement.addBatch();
                if ((counter + 1) % PG_BATCH_SIZE == 0 || (counter + 1) == actorData.size()) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
