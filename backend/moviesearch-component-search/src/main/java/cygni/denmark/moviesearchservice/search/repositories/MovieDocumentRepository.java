package cygni.denmark.moviesearchservice.search.repositories;

import cygni.denmark.moviesearchservice.search.documents.MovieDocument;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface MovieDocumentRepository extends ReactiveElasticsearchRepository<MovieDocument, String> {
}
