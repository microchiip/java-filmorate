package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {

    public static final Map<Integer, Genre> GENRE_DATA = new LinkedHashMap<>();

    static {
        GENRE_DATA.put(1, new Genre(1, "Комедия"));
        GENRE_DATA.put(2, new Genre(2, "Драма"));
        GENRE_DATA.put(3, new Genre(3, "Мультфильм"));
        GENRE_DATA.put(4, new Genre(4, "Триллер"));
        GENRE_DATA.put(5, new Genre(5, "Документальный"));
        GENRE_DATA.put(6, new Genre(6, "Боевик"));
    }

    @GetMapping
    public Collection<Genre> findAll() {
        return GENRE_DATA.values();
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable int id) {
        Genre genre = GENRE_DATA.get(id);
        if (genre == null) {
            throw new NotFoundException("Жанр с id=" + id + " не найден");
        }
        return genre;
    }
}
