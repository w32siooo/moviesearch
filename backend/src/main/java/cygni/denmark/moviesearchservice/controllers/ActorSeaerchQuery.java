package cygni.denmark.moviesearchservice.controllers;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ActorSeaerchQuery {
    private UUID id;
    private String nconst;
    private String primaryName;
    private String birthYear;
    private String deathYear;
    private List<String> primaryProfession;
    private List<String> knownForTitles;
}
