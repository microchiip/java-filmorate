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
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.DataStore;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final DataStore store;

    @GetMapping
    public List<Review> findAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        return store.reviews.values().stream()
                .filter(r -> filmId == null || r.getFilmId().equals(filmId))
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Review findById(@PathVariable Long id) {
        return getOrThrow(id);
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        if (!store.users.containsKey(review.getUserId())) {
            throw new NotFoundException("Пользователь с id=" + review.getUserId() + " не найден");
        }
        if (!store.films.containsKey(review.getFilmId())) {
            throw new NotFoundException("Фильм с id=" + review.getFilmId() + " не найден");
        }
        review.setReviewId(store.nextReviewId());
        store.reviews.put(review.getReviewId(), review);
        store.events.add(new Event(store.nextEventId(), review.getUserId(),
                System.currentTimeMillis(), "REVIEW", "ADD", review.getReviewId()));
        log.info("Создан отзыв: {}", review);
        return review;
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        Review existing = getOrThrow(review.getReviewId());
        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        store.events.add(new Event(store.nextEventId(), existing.getUserId(),
                System.currentTimeMillis(), "REVIEW", "UPDATE", existing.getReviewId()));
        log.info("Обновлён отзыв: {}", existing);
        return existing;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Review review = getOrThrow(id);
        store.reviews.remove(id);
        store.events.add(new Event(store.nextEventId(), review.getUserId(),
                System.currentTimeMillis(), "REVIEW", "REMOVE", id));
        log.info("Удалён отзыв id={}", id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Review addLike(@PathVariable Long id, @PathVariable Long userId) {
        Review review = getOrThrow(id);
        review.setUseful(review.getUseful() + 1);
        return review;
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Review removeLike(@PathVariable Long id, @PathVariable Long userId) {
        Review review = getOrThrow(id);
        review.setUseful(review.getUseful() - 1);
        return review;
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review addDislike(@PathVariable Long id, @PathVariable Long userId) {
        Review review = getOrThrow(id);
        review.setUseful(review.getUseful() - 1);
        return review;
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Review removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        Review review = getOrThrow(id);
        review.setUseful(review.getUseful() + 1);
        return review;
    }

    private Review getOrThrow(Long id) {
        Review review = store.reviews.get(id);
        if (review == null) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
        return review;
    }
}
