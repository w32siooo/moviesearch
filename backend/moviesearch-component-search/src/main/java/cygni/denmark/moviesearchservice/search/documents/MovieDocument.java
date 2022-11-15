package cygni.denmark.moviesearchservice.search.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "movies")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Version
    @Field(type = FieldType.Long)
    private Long version;

    @Field(type = FieldType.Text)
    private String tconst;

    @Field(type = FieldType.Text)
    private String titleType;

    @Field(type = FieldType.Text)
    private String primaryTitle;

    @Field(type = FieldType.Text)
    private String originalTitle;

    @Field(type = FieldType.Integer)
    private Integer startYear;

    @Field(type = FieldType.Integer)
    private Integer endYear;

    @Field(type = FieldType.Integer)
    private Integer runtimeMinutes;

    @Field(type = FieldType.Auto)
    private List<String> genres;
}
