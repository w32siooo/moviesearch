package cygni.denmark.moviesearchservice.controllers;

import cygni.denmark.moviesearchservice.persistence.repositories.ActorRepository;
import cygni.denmark.moviesearchservice.persistence.repositories.MovieRepository;
import cygni.denmark.moviesearchservice.persistence.repositories.StateValue;
import cygni.denmark.moviesearchservice.services.StateService;
import cygni.denmark.moviesearchservice.tasks.IngestDirigentTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/state")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "cygni", name = "ingest", havingValue = "true")
class StateController {

    private final StateService stateService;

    private final IngestDirigentTask ingestDirigentTask;

    private final MovieRepository movieRepository;

    private final ActorRepository actorRepository;

     @GetMapping(value = "", produces = "application/json")
    public Mono<StateValue> getState() {
        return stateService.getState();
    }

    @GetMapping(value = "ingest", produces = "application/json")
    public Mono<String> updateState() {
         ingestDirigentTask.scheduleFixedRateWithInitialDelayTask();
         return Mono.just("Scheduled");
    }

    @GetMapping(value = "actorCount", produces = "application/json")
    public Mono<Long> getActorCount() {
        return Mono.fromCallable(actorRepository::count)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(value = "movieCount", produces = "application/json")
    public Mono<Long> getMovieCount() {
        return Mono.fromCallable(movieRepository::count)
                .subscribeOn(Schedulers.boundedElastic());
    }

 }
