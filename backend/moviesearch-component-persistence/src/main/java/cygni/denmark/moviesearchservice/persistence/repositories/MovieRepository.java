package cygni.denmark.moviesearchservice.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MovieRepository extends JpaRepository<MovieDb, UUID> {

  MovieDb findByTconst(String tconst);
}
