package cygni.denmark.moviesearchservice.tasks;

import cygni.denmark.moviesearchservice.persistence.repositories.MovieDb;
import cygni.denmark.moviesearchservice.search.documents.MovieDocument;
import cygni.denmark.moviesearchservice.search.repositories.MovieDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.util.set.Sets;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.BaseStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovieDocIngestTask {

  private final ModelMapper modelMapper;
  private final MovieDocumentRepository movieDocumentRepository;
  private final JdbcTemplate jdbcTemplate;
  private final String postgresToElasticQuery =
      """
            SELECT
                id,
                version,
                timestamp,
                tconst,
                title_type,
                primary_title,
                original_title,
                start_year,
                end_year,
                runtime_minutes,
                STRING_AGG (genres, ',') genres_list
            FROM
                movies
                    INNER JOIN genres gg on movies.id = gg.movie_db_id
            GROUP BY
                id
            ORDER BY
                id;
            """;

  @Value("${cygni.elasticWindow}")
  public Integer elasticWindowSize;

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  public void streamToElasticFromJpaAndBlock() {
    Flux.defer(
            () ->
                Flux.fromStream(
                    jdbcTemplate.queryForStream(
                        postgresToElasticQuery,
                        (resultSet, rowNum) ->
                            new MovieDb(
                                resultSet.getObject(1, UUID.class),
                                resultSet.getLong(2),
                                resultSet.getTimestamp(3),
                                resultSet.getString(4),
                                resultSet.getString(5),
                                resultSet.getString(6),
                                resultSet.getString(7),
                                resultSet.getInt(8),
                                resultSet.getInt(9),
                                resultSet.getInt(10),
                                Sets.newHashSet(resultSet.getString(10).split(","))))))
        .map(mov -> modelMapper.map(mov, MovieDocument.class))
        .window(elasticWindowSize) // Splits flux into multiple windows so elastic doesn't choke.
        .flatMap(movieDocumentRepository::saveAll, 4)
        .onErrorResume(s -> Mono.empty())
        .count()
        .block();
    log.info("Movie elements also indexed in elastic");
  }
}
