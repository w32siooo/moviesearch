package cygni.denmark.moviesearchservice.search.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Actor {
  private UUID id;
  private String nconst;
  private String primaryName;
  private String birthYear;
  private String deathYear;
  private List<String> primaryProfession;
  private List<String> knownForTitles;
}
