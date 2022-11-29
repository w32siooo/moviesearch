package cygni.denmark.moviesearchservice.controllers;

import cygni.denmark.moviesearchservice.dtos.CountDTO;
import cygni.denmark.moviesearchservice.tasks.IngestDirigentTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class UtilController {
    private final IngestDirigentTask ingestDirigentTask;

    @Value("${cygni.secret}")
    private String secret;

    @GetMapping(value = "ingest", produces = "application/json")
    public Mono<ResponseEntity<String>> count(@RequestParam String sec) {
        if(secret.equals(sec)){
            ingestDirigentTask.scheduleFixedRateWithInitialDelayTask();
            return Mono.just(ResponseEntity.ok("scheduled"));
        }
        return Mono.just(ResponseEntity.ok("wrong secret"));
    }
}
