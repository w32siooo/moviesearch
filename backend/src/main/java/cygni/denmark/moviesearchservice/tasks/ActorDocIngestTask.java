package cygni.denmark.moviesearchservice.tasks;

import cygni.denmark.moviesearchservice.persistence.repositories.ActorDb;
import cygni.denmark.moviesearchservice.search.documents.ActorDocument;
import cygni.denmark.moviesearchservice.search.repositories.ActorDocumentRepository;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class ActorDocIngestTask {

  private final ModelMapper modelMapper;
  private final ActorDocumentRepository actorDocumentRepository;
  private final JdbcTemplate jdbcTemplate;
  private final String postgresToElasticQuery =
      """
            SELECT id,
                   version,
                   timestamp,
                   nconst,
                   primary_name,
                   birth_year,
                   death_year,
                   STRING_AGG(known_for_titles, ',')   known_for_titles_list,
                   STRING_AGG(primary_profession, ',') primary_professions_list
            FROM actors
                     LEFT JOIN known_for_titles kft on actors.id = kft.actor_db_id
                     LEFT JOIN primary_profession pp on actors.id = pp.actor_db_id
            GROUP BY id
            """;

  @Value("${cygni.elasticWindow}")
  public Integer elasticWindowSize;

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  public ActorDocument streamToElasticFromJpaAndBlock() {

    return Flux.defer(
            () ->
                Flux.fromStream(
                    jdbcTemplate.queryForStream(
                        postgresToElasticQuery,
                        (resultSet, rowNum) ->
                            new ActorDb(
                                resultSet.getObject(1, UUID.class),
                                resultSet.getLong(2),
                                resultSet.getTimestamp(3),
                                resultSet.getString(4),
                                resultSet.getString(5),
                                resultSet.getInt(6),
                                resultSet.getInt(7),
                                Sets.newHashSet(resultSet.getString(8).split(",")),
                                Sets.newHashSet(resultSet.getString(9).split(","))))))
        .map(actorDb -> modelMapper.map(actorDb, ActorDocument.class))
        .window(elasticWindowSize) // Splits flux into multiple windows so elastic doesn't choke.
        .flatMap(actorDocumentRepository::saveAll, 4)
        .onErrorResume(s -> Mono.empty())
        .blockLast();
  }

  private Mono<Long> directIndexActorsFlux(Flux<ActorDb> actorsFlux) {

    return actorsFlux
        .map(actorDb -> modelMapper.map(actorDb, ActorDocument.class))
        .window(elasticWindowSize) // Splits flux into multiple windows so elastic doesn't choke.
        .flatMap(actorDocumentRepository::saveAll)
        .count();
  }
}
