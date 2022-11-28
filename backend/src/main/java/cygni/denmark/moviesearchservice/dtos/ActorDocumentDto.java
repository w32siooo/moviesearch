package cygni.denmark.moviesearchservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActorDocumentDto {
  private UUID id;
  private String nconst;
  private String primaryName;
  private String birthYear;
  private String deathYear;
  private List<String> primaryProfession;
  private List<String> knownForTitles;
  private Map<String, String> titleMappings = new HashMap<>();
}
