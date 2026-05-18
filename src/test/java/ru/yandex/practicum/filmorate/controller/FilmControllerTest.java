package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("Sci-fi");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }

    @Test
    void createAssignsIdAndStoresFilm() {
        Film created = controller.create(validFilm());
        assertNotNull(created.getId());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void releaseDateBeforeCinemaBirthdayFails() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void releaseDateEqualToCinemaBirthdayIsValid() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        Film created = controller.create(film);
        assertNotNull(created.getId());
    }

    @Test
    void updateWithoutIdFails() {
        Film film = validFilm();
        assertThrows(ValidationException.class, () -> controller.update(film));
    }

    @Test
    void updateWithUnknownIdFails() {
        Film film = validFilm();
        film.setId(999L);
        assertThrows(ValidationException.class, () -> controller.update(film));
    }

    @Test
    void updateChangesStoredFilm() {
        Film created = controller.create(validFilm());
        created.setName("Inception (Director's Cut)");
        Film updated = controller.update(created);
        assertEquals("Inception (Director's Cut)", updated.getName());
    }
}
