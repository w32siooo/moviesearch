package cygni.denmark.moviesearchservice.search.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.sql.Timestamp;
import java.util.List;

@Document(indexName = "actors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActorDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Version
    @Field(type = FieldType.Long)
    private Long version;

    @Field(type = FieldType.Text)
    private String nconst;

    @Field(type = FieldType.Text)
    private String primaryName;

    @Field(type = FieldType.Integer)
    private Integer birthYear;

    @Field(type = FieldType.Integer)
    private Integer deathYear;

    @Field(type = FieldType.Auto)
    private List<String> primaryProfession;

    @Field(type = FieldType.Auto)
    private List<String> knownForTitles;

    @Field(type = FieldType.Auto)
    private Timestamp timestamp;


}
