package cygni.denmark.moviesearchservice.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StateRepository extends JpaRepository<StateDb, String> {

  StateDb findByState(StateValue state);
}

