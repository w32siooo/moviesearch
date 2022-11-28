package cygni.denmark.moviesearchservice.search.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.denmark.moviesearchservice.search.documents.MovieDocument;
import cygni.denmark.moviesearchservice.search.models.Movie;
import cygni.denmark.moviesearchservice.search.repositories.MovieDocumentRepository;
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
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieSearchService {

  private final ReactiveElasticsearchClient reactiveElasticsearchClient;

  private final ObjectMapper objectMapper;

  private final MovieDocumentRepository movieDocumentRepository;

  public Mono<Long> count() {
    return movieDocumentRepository.count();
  }

  public Flux<MovieDocument> freeSearchMovies(String searchValue) {
    SearchRequest searchRequest =
        searchRequest("movies"); // Without arguments runs against all indices
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

    NativeSearchQuery searchQuery =
        new NativeSearchQueryBuilder()
            .withQuery(
                multiMatchQuery(searchValue)
                    .field("primaryTitle")
                    .field("originalTitle")
                    .field("titleType")
                    .field("genres")
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS))
            .build();

    sourceBuilder.query(searchQuery.getQuery());
    searchRequest.source(sourceBuilder);

    return reactiveElasticsearchClient
        .search(searchRequest)
        .map(hit -> objectMapper.convertValue(hit.getSourceAsMap(), MovieDocument.class))
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("found nothing")));
  }

  public Flux<MovieDocument> searchMovies(Movie searchValue) {

    SearchRequest searchRequest =
        searchRequest("actors"); // Without arguments runs against all indices
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

    if (searchValue.getGenres() != null)
      sourceBuilder.query(QueryBuilders.matchQuery("genres", searchValue.getGenres()));

    searchRequest.source(sourceBuilder);

    return reactiveElasticsearchClient
        .search(searchRequest)
        .map(hit -> objectMapper.convertValue(hit.getSourceAsMap(), MovieDocument.class))
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("found nothing")));
  }
}
