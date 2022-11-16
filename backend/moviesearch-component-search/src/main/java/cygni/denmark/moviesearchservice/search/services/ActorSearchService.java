package cygni.denmark.moviesearchservice.search.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.denmark.moviesearchservice.search.documents.ActorDocument;
import cygni.denmark.moviesearchservice.search.models.Actor;
import cygni.denmark.moviesearchservice.search.repositories.ActorDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.elasticsearch.client.Requests.searchRequest;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActorSearchService {

  private final ReactiveElasticsearchClient reactiveElasticsearchClient;

  private final ObjectMapper objectMapper;

  private final ActorDocumentRepository actorDocumentRepository;

  public Mono<Long> count() {
    return actorDocumentRepository.count();
  }

  public Flux<ActorDocument> freeSearchActors(String searchValue) {
    SearchRequest searchRequest =
        searchRequest("actors"); // Without arguments runs against all indices
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

    if (searchValue.matches("\\d{4}")) {
      sourceBuilder.query(QueryBuilders.matchQuery("birthYear", Integer.parseInt(searchValue)));
    } else {
      NativeSearchQuery searchQuery =
          new NativeSearchQueryBuilder()
              .withQuery(
                  multiMatchQuery(searchValue)
                      .field("primaryName")
                      .field("primaryProfession")
                      .field("knownForTitles")
                      .field("nconst")
                      .type(MultiMatchQueryBuilder.Type.MOST_FIELDS))
              .build();
      sourceBuilder.query(searchQuery.getQuery());
    }

    searchRequest.source(sourceBuilder);

    return reactiveElasticsearchClient
        .search(searchRequest)
        .map(hit -> objectMapper.convertValue(hit.getSourceAsMap(), ActorDocument.class));
  }

  public Flux<ActorDocument> searchActors(Actor searchValue) {

    SearchRequest searchRequest =
        searchRequest("actors"); // Without arguments runs against all indices
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

    if (searchValue.getBirthYear() != null)
      sourceBuilder.query(QueryBuilders.matchQuery("birthYear", searchValue.getBirthYear()));
    if (searchValue.getDeathYear() != null)
      sourceBuilder.query(QueryBuilders.matchQuery("deathYear", searchValue.getDeathYear()));
    if (searchValue.getPrimaryName() != null)
      sourceBuilder.query(QueryBuilders.matchQuery("primaryName", searchValue.getPrimaryName()));
    if (searchValue.getPrimaryProfession() != null)
      sourceBuilder.query(
          QueryBuilders.matchQuery("primaryProfession", searchValue.getPrimaryProfession()));
    if (searchValue.getKnownForTitles() != null)
      sourceBuilder.query(
          QueryBuilders.matchQuery("knownForTitles", searchValue.getKnownForTitles()));

    searchRequest.source(sourceBuilder);

    return reactiveElasticsearchClient
        .search(searchRequest)
        .map(hit -> objectMapper.convertValue(hit.getSourceAsMap(), ActorDocument.class));
  }
}
