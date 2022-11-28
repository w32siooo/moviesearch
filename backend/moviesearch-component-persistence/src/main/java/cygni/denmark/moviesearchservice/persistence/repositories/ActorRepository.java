package cygni.denmark.moviesearchservice.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActorRepository extends JpaRepository<ActorDb, UUID> {}
