package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    public FilmController() {
        // Пустой конструктор для тестов
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        Film film = films.get(id);
        if (film == null) {
            log.warn("Фильм с id={} не найден", id);
            throw new ValidationException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateReleaseDate(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            log.warn("Не указан id фильма при обновлении");
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new ValidationException("Фильм с id=" + film.getId() + " не найден");
        }
        validateReleaseDate(film);
        films.put(film.getId(), film);
        log.info("Обновлён фильм: {}", film);
        return film;
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {

        log.info("Запрос популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);

        List<Film> allFilms = new ArrayList<>(films.values());

        List<Film> filteredByYear = allFilms.stream()
                .filter(film -> {
                    if (year != null) {
                        return film.getReleaseDate() != null &&
                                film.getReleaseDate().getYear() == year;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<Film> filteredByGenre = filteredByYear;
        if (genreId != null) {
            log.debug("Фильтрация по жанру id={} временно не поддерживается", genreId);
        }

        List<Film> popularFilms = filteredByGenre.stream()
                .sorted((f1, f2) -> {

                    return Long.compare(f2.getId(), f1.getId());
                })
                .limit(count)
                .collect(Collectors.toList());

        return popularFilms;
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Дата релиза фильма раньше 28.12.1895: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }
}
