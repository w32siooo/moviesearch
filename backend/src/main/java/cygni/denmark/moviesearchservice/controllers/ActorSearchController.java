package cygni.denmark.moviesearchservice.controllers;

import cygni.denmark.moviesearchservice.dtos.CountDTO;
import cygni.denmark.moviesearchservice.search.documents.ActorDocument;
import cygni.denmark.moviesearchservice.search.models.Actor;
import cygni.denmark.moviesearchservice.search.services.ActorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/actors")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ActorSearchController {
    private final ActorSearchService actorSearchService;
    private final ModelMapper modelMapper;

    @GetMapping(value = "", produces = "application/json")
    public Mono<ResponseEntity<List<ActorDocument>>> findActors(@ModelAttribute ActorSeaerchQuery actorSeaerchQuery) {
        return actorSearchService.searchActors(modelMapper.map(actorSeaerchQuery, Actor.class))
                .collectList().map(ResponseEntity::ok);
    }

    @GetMapping(value = "count", produces = "application/json")
    public Mono<ResponseEntity<CountDTO>> count() {
        return actorSearchService.count().map(count -> ResponseEntity.ok(new CountDTO(count)));
    }

    @GetMapping(value = "freeSearch", produces = "application/json")
    public Mono<ResponseEntity<List<ActorDocument>>> findActorsFree(@RequestParam String q) {
        return actorSearchService
                .freeSearchActors(q)
                .collectList()
                .filter(list->!list.isEmpty())
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

}
