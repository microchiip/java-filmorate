package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("Sci-fi film about dreams");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }

    @Test
    void validFilmHasNoViolations() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm());
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankNameFailsValidation() {
        Film film = validFilm();
        film.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullNameFailsValidation() {
        Film film = validFilm();
        film.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void descriptionExactly200CharsIsValid() {
        Film film = validFilm();
        film.setDescription("a".repeat(200));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void descriptionLongerThan200CharsFails() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    void zeroDurationFailsValidation() {
        Film film = validFilm();
        film.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void negativeDurationFailsValidation() {
        Film film = validFilm();
        film.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }
}
