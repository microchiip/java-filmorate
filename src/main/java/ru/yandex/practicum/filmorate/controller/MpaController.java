package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {

    public static final Map<Integer, Mpa> MPA_DATA = new LinkedHashMap<>();

    static {
        MPA_DATA.put(1, new Mpa(1, "G"));
        MPA_DATA.put(2, new Mpa(2, "PG"));
        MPA_DATA.put(3, new Mpa(3, "PG-13"));
        MPA_DATA.put(4, new Mpa(4, "R"));
        MPA_DATA.put(5, new Mpa(5, "NC-17"));
    }

    @GetMapping
    public Collection<Mpa> findAll() {
        return MPA_DATA.values();
    }

    @GetMapping("/{id}")
    public Mpa findById(@PathVariable int id) {
        Mpa mpa = MPA_DATA.get(id);
        if (mpa == null) {
            throw new NotFoundException("Рейтинг с id=" + id + " не найден");
        }
        return mpa;
    }
}
