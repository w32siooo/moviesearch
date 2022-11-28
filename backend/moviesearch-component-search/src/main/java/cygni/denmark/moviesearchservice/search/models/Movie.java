package cygni.denmark.moviesearchservice.search.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Movie {
  private UUID id;
  private String tconst;

  private String titleType;

  private String primaryTitle;

  private String originalTitle;

  private Boolean isAdult;

  private Integer startYear;

  private Integer endYear;

  private Integer runtimeMinutes;

  private List<String> genres;
}
