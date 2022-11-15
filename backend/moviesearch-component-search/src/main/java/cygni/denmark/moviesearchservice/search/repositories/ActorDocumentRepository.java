package cygni.denmark.moviesearchservice.search.repositories;

import cygni.denmark.moviesearchservice.search.documents.ActorDocument;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface ActorDocumentRepository extends ReactiveElasticsearchRepository<ActorDocument, String> {
}
