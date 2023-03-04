package com.example.bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.AllArgsConstructor;

import java.util.Date;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "Diary")
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //Название добавляемого тайтла
    @NotNull
    private String nameTitle;

    //Название для категории: полный метр, короткий метр, сериал, аниме, фильм и.т.д.
    @NotNull
    private String category;

    //Оценка в виде 0.0
    @NotNull
    private Double rating;

    //Входит ли в избранное
    private Boolean favourites;

    //Строка для описания
    private String description;

    //Дата создание записи
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date DateAdd;

    public Diary(){

    }
}
