package com.muyulu.mboard.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "albums")
public class AlbumSearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long albumId;

    @Field(type = FieldType.Long)
    private Long artistId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(name = "genre", type = FieldType.Keyword)
    private List<String> genres;

    @Field(type = FieldType.Keyword)
    private String artistName;

    @Field(type = FieldType.Keyword)
    private String coverUrl;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String summary;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate releaseDate;

    @Field(type = FieldType.Boolean)
    private Boolean trending;

    @Field(type = FieldType.Boolean)
    private Boolean published;
}
