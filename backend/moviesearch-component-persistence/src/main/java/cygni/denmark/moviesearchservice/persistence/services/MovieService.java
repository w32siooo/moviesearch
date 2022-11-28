package cygni.denmark.moviesearchservice.persistence.services;

import cygni.denmark.moviesearchservice.persistence.repositories.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MovieService {

  private final MovieRepository movieRepository;

  public String findMovieTitleByTConst(String tConst) {
    var mov = movieRepository.findByTconst(tConst);
    if (mov == null) {
      return tConst;
    }
    return mov.getOriginalTitle();
  }
}
