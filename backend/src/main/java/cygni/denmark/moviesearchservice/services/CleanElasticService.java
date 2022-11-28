package cygni.denmark.moviesearchservice.services;

import cygni.denmark.moviesearchservice.search.repositories.ActorDocumentRepository;
import cygni.denmark.moviesearchservice.search.repositories.MovieDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CleanElasticService {
  private final ActorDocumentRepository actorDocumentRepository;

  private final MovieDocumentRepository movieDocumentRepository;

  public Mono<Void> cleanRepos() {
    return Mono.zip(movieDocumentRepository.deleteAll(), actorDocumentRepository.deleteAll())
        .then();
  }
}
