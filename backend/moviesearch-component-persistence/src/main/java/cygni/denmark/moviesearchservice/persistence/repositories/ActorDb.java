package cygni.denmark.moviesearchservice.persistence.repositories;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "actors")
public class ActorDb {
  @Id private UUID id;
  @Version private Long version;
  private Timestamp timestamp;
  private String nconst;
  private String primaryName;
  private Integer birthYear;
  private Integer deathYear;

  @ElementCollection
  @CollectionTable(name = "known_for_titles")
  private Set<String> knownForTitles = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "primary_profession")
  private Set<String> primaryProfession = new HashSet<>();
}
