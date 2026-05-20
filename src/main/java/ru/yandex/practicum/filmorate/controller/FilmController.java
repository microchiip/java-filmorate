package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.DataStore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final DataStore store;

    @GetMapping
    public Collection<Film> findAll() {
        return store.films.values();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        return getOrThrow(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateReleaseDate(film);
        film.setId(store.nextFilmId());
        resolveReferences(film);
        store.films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!store.films.containsKey(film.getId())) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        validateReleaseDate(film);
        film.setLikes(store.films.get(film.getId()).getLikes());
        resolveReferences(film);
        store.films.put(film.getId(), film);
        log.info("Обновлён фильм: {}", film);
        return film;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        getOrThrow(id);
        store.films.remove(id);
        log.info("Удалён фильм id={}", id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        Film film = getOrThrow(id);
        if (!store.users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        film.getLikes().add(userId);
        store.events.add(new Event(
                store.nextEventId(), userId, System.currentTimeMillis(), "LIKE", "ADD", id));
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
        return film;
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable Long id, @PathVariable Long userId) {
        Film film = getOrThrow(id);
        if (!store.users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        film.getLikes().remove(userId);
        store.events.add(new Event(
                store.nextEventId(), userId, System.currentTimeMillis(), "LIKE", "REMOVE", id));
        log.info("Пользователь {} убрал лайк у фильма {}", userId, id);
        return film;
    }

    @GetMapping("/popular")
    public List<Film> getPopular(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        return store.films.values().stream()
                .filter(f -> genreId == null || f.getGenres().stream().anyMatch(g -> g.getId() == genreId))
                .filter(f -> year == null || (f.getReleaseDate() != null && f.getReleaseDate().getYear() == year))
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getByDirector(@PathVariable Long directorId, @RequestParam(defaultValue = "likes") String sortBy) {
        if (!store.directors.containsKey(directorId)) {
            throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");
        }
        List<Film> result = store.films.values().stream()
                .filter(f -> f.getDirectors().stream().anyMatch(d -> d.getId().equals(directorId)))
                .collect(Collectors.toList());
        if ("year".equals(sortBy)) {
            result.sort(Comparator.comparing(f -> f.getReleaseDate() == null ? LocalDate.MIN : f.getReleaseDate()));
        } else {
            result.sort(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed());
        }
        return result;
    }

    @GetMapping("/search")
    public List<Film> search(@RequestParam String query, @RequestParam String by) {
        String lowerQuery = query.toLowerCase();
        List<String> byList = List.of(by.split(","));
        boolean byTitle = byList.contains("title");
        boolean byDirector = byList.contains("director");

        return store.films.values().stream()
                .filter(f -> {
                    boolean matchTitle = byTitle && f.getName() != null && f.getName().toLowerCase().contains(lowerQuery);
                    boolean matchDirector = byDirector && f.getDirectors().stream()
                            .anyMatch(d -> d.getName() != null && d.getName().toLowerCase().contains(lowerQuery));
                    return matchTitle || matchDirector;
                })
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .collect(Collectors.toList());
    }

    @GetMapping("/common")
    public List<Film> getCommon(@RequestParam Long userId, @RequestParam Long friendId) {
        if (!store.users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!store.users.containsKey(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }
        return store.films.values().stream()
                .filter(f -> f.getLikes().contains(userId) && f.getLikes().contains(friendId))
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .collect(Collectors.toList());
    }

    private Film getOrThrow(Long id) {
        Film film = store.films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    private void resolveReferences(Film film) {
        if (film.getMpa() != null) {
            Mpa resolved = MpaController.MPA_DATA.get(film.getMpa().getId());
            if (resolved != null) {
                film.setMpa(new Mpa(resolved.getId(), resolved.getName()));
            }
        }
        if (film.getGenres() != null) {
            Map<Integer, Genre> deduped = new LinkedHashMap<>();
            for (Genre g : film.getGenres()) {
                Genre resolved = GenreController.GENRE_DATA.get(g.getId());
                if (resolved != null) {
                    deduped.put(g.getId(), new Genre(resolved.getId(), resolved.getName()));
                } else {
                    deduped.put(g.getId(), g);
                }
            }
            film.setGenres(new ArrayList<>(deduped.values()));
        }
        if (film.getDirectors() != null) {
            List<Director> resolved = new ArrayList<>();
            for (Director d : film.getDirectors()) {
                Director stored = store.directors.get(d.getId());
                if (stored != null) {
                    Director copy = new Director();
                    copy.setId(stored.getId());
                    copy.setName(stored.getName());
                    resolved.add(copy);
                }
            }
            film.setDirectors(resolved);
        }
        if (film.getGenres() == null) {
            film.setGenres(new ArrayList<>());
        }
        if (film.getDirectors() == null) {
            film.setDirectors(new ArrayList<>());
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Дата релиза фильма раньше 28.12.1895: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }
}
