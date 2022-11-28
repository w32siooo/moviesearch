package cygni.denmark.moviesearchservice.persistence.repositories;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movies")
public class MovieDb {
  @Id private UUID id;
  @Version private Long version;
  private Timestamp timestamp;

  private String tconst;

  private String titleType;

  private String primaryTitle;

  private String originalTitle;

  private Integer startYear;

  private Integer endYear;

  private Integer runtimeMinutes;

  @ElementCollection
  @CollectionTable(name = "genres")
  private Set<String> genres;

  public void bindToPreparedStatement(PreparedStatement statement) throws SQLException {
    statement.setObject(1, id);
    statement.setInt(2, endYear);
    statement.setString(
        3, originalTitle.length() > 30 ? originalTitle.substring(0, 30) : originalTitle);
    statement.setString(4, primaryTitle);
    statement.setInt(5, runtimeMinutes);
    statement.setInt(6, startYear);
    statement.setString(7, tconst);
    statement.setString(8, titleType);
    statement.setLong(9, version);
  }
}
