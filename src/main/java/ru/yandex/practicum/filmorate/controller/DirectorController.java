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
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DataStore;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DataStore store;

    @GetMapping
    public Collection<Director> findAll() {
        return store.directors.values();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable Long id) {
        return getOrThrow(id);
    }

    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        director.setId(store.nextDirectorId());
        store.directors.put(director.getId(), director);
        log.info("Создан режиссёр: {}", director);
        return director;
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        if (director.getId() == null || !store.directors.containsKey(director.getId())) {
            throw new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
        }
        store.directors.put(director.getId(), director);
        log.info("Обновлён режиссёр: {}", director);
        return director;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        getOrThrow(id);
        store.directors.remove(id);
        for (Film film : store.films.values()) {
            film.getDirectors().removeIf(d -> d.getId().equals(id));
        }
        log.info("Удалён режиссёр id={}", id);
    }

    private Director getOrThrow(Long id) {
        Director d = store.directors.get(id);
        if (d == null) {
            throw new NotFoundException("Режиссёр с id=" + id + " не найден");
        }
        return d;
    }
}
