package cygni.denmark.moviesearchservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.denmark.moviesearchservice.dtos.CountDTO;
import cygni.denmark.moviesearchservice.persistence.repositories.MovieDb;
import cygni.denmark.moviesearchservice.persistence.services.MovieService;
import cygni.denmark.moviesearchservice.search.documents.ActorDocument;
import cygni.denmark.moviesearchservice.search.models.Actor;
import cygni.denmark.moviesearchservice.search.services.ActorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.util.List;

@RestController
@RequestMapping("/api/actors")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ActorSearchController {
  private final ActorSearchService actorSearchService;
  private final ModelMapper modelMapper;

  private final MovieService movieService;

  private final ObjectMapper objectMapper;

  @GetMapping(value = "", produces = "application/json")
  public Mono<ResponseEntity<List<ActorDocument>>> findActors(
      @ModelAttribute ActorSeaerchQuery actorSeaerchQuery) {
    return actorSearchService
        .searchActors(modelMapper.map(actorSeaerchQuery, Actor.class))
        .collectList()
        .map(ResponseEntity::ok);
  }

  @GetMapping(value = "count", produces = "application/json")
  public Mono<ResponseEntity<CountDTO>> count() {
    return actorSearchService.count().map(count -> ResponseEntity.ok(new CountDTO(count)));
  }

  @GetMapping(value = "freeSearch", produces = "application/json")
  public Mono<ResponseEntity<List<ActorDocumentDto>>> findActorsFree(@RequestParam String q) {
    return actorSearchService
        .freeSearchActors(q)
        .map(actorDoc -> modelMapper.map(actorDoc, ActorDocumentDto.class))
        .flatMap(
            actorDoc ->
                Mono.zip(
                        Flux.fromIterable(actorDoc.getKnownForTitles())
                            .flatMap(
                                tconst -> // map all titles
                                    Mono.zip(
                                        Mono.fromCallable(
                                                () -> movieService.findMovieTitleByTConst(tconst))
                                            .subscribeOn(Schedulers.boundedElastic()),
                                        Mono.just(tconst)))
                            .collectMap(Tuple2::getT2, Tuple2::getT1),
                        Mono.just(actorDoc))
                    .map(
                        tuple -> {
                          tuple.getT2().setTitleMappings(tuple.getT1());
                          return tuple.getT2();
                        }))
        .collectList()
        .filter(list -> !list.isEmpty())
        .map(ResponseEntity::ok)
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }
}
