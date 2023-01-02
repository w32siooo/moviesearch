package cygni.denmark.moviesearchservice.persistence.repositories;

public enum StateValue {
    INGESTING,
    ACTORS_INGESTED_PG,
    MOVIES_INGESTED_PG,
    ACTORS_INGESTED_ES,
    MOVIES_INGESTED_ES,
    FINISHED
}
