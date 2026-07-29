package com.bookstore.catalog.search;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.UUID;

@Document(indexName = "books")
@Getter
@Setter
public class BookDocument {

    @Id
    private UUID id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String author;

    @Field(type = FieldType.Keyword)
    private String genre;

    @Field(type = FieldType.Keyword)
    private String language;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Date)
    private LocalDate publishedDate;

    @Field(type = FieldType.Double)
    private double averageRating;

    @Field(type = FieldType.Long)
    private long reviewCount;

    public BookDocument() {
    }

    public BookDocument(UUID id, String title, String author, String genre, String language,
                        String description, LocalDate publishedDate, double averageRating, long reviewCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.language = language;
        this.description = description;
        this.publishedDate = publishedDate;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }
}