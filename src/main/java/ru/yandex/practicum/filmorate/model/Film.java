package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Film.
 */
@Data
public class Film {
    private Long id;
    private Mpa mpa;

    @Data
    public static class Mpa {
        private int id;
        private String name;
    }

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

    // Добавьте эти поля для тестов следующего спринта
    private List<Genre> genres = new ArrayList<>();
    private List<Director> directors = new ArrayList<>();

    // Вспомогательные классы (можно создать отдельными файлами)
    @Data
    public static class Genre {
        private int id;
        private String name;
    }

    @Data
    public static class Director {
        private int id;
        private String name;
    }
}
