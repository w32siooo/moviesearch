package cygni.denmark.moviesearchservice.persistence.repositories;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movies")
public class MovieDb {
    @Id
    private UUID id;
    @Version
    private Long version;
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
}
