package cygni.denmark.moviesearchservice.controllers;

import cygni.denmark.moviesearchservice.dtos.CountDTO;
import cygni.denmark.moviesearchservice.search.documents.ActorDocument;
import cygni.denmark.moviesearchservice.search.documents.MovieDocument;
import cygni.denmark.moviesearchservice.search.models.Movie;
import cygni.denmark.moviesearchservice.search.services.MovieSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class MovieSearchController {
    private final MovieSearchService movieSearchService;
    private final ModelMapper modelMapper;

    @GetMapping(value = "", produces = "application/json")
    public Mono<ResponseEntity<List<MovieDocument>>> findMovies(@ModelAttribute ActorSeaerchQuery actorSeaerchQuery) {
        return movieSearchService.searchMovies(modelMapper.map(actorSeaerchQuery, Movie.class))
                .collectList().map(ResponseEntity::ok);
    }

    @GetMapping(value = "count", produces = "application/json")
    public Mono<ResponseEntity<CountDTO>> count() {
        return movieSearchService.count().map(count -> ResponseEntity.ok(new CountDTO(count)));
    }

    @GetMapping(value = "freeSearch", produces = "application/json")
    public Mono<ResponseEntity<List<MovieDocument>>> findMoviesFree(@RequestParam String q) {
        return movieSearchService.freeSearchMovies(q)
                .collectList()
                .map(ResponseEntity::ok);
    }

}
