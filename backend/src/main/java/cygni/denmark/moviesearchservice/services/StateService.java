package cygni.denmark.moviesearchservice.services;

import cygni.denmark.moviesearchservice.persistence.repositories.StateDb;
import cygni.denmark.moviesearchservice.persistence.repositories.StateRepository;
import cygni.denmark.moviesearchservice.persistence.repositories.StateValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class StateService {

  private final StateRepository stateRepository;

  private final CleanElasticService cleanElasticService;

  public Mono<StateValue> getState() {
    return Mono.fromCallable(
            () ->
                stateRepository
                    .findById("0")
                    .orElseGet(
                        () ->
                           stateRepository.save(new StateDb("0", StateValue.INGESTING))
                        )
                    .getState())
        .subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<StateValue> updateState(StateValue state) {
    return Mono.fromCallable(() -> stateRepository.save(new StateDb("0", state)).getState())
        .subscribeOn(Schedulers.boundedElastic());
  }
}
