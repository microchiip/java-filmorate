package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DataStore {
    public final Map<Long, Film> films = new HashMap<>();
    public final Map<Long, User> users = new HashMap<>();
    public final Map<Long, Director> directors = new HashMap<>();
    public final Map<Long, Review> reviews = new HashMap<>();
    public final List<Event> events = new ArrayList<>();

    private final AtomicLong nextFilmId = new AtomicLong(1);
    private final AtomicLong nextUserId = new AtomicLong(1);
    private final AtomicLong nextDirectorId = new AtomicLong(1);
    private final AtomicLong nextReviewId = new AtomicLong(1);
    private final AtomicLong nextEventId = new AtomicLong(1);

    public long nextFilmId() {
        return nextFilmId.getAndIncrement();
    }

    public long nextUserId() {
        return nextUserId.getAndIncrement();
    }

    public long nextDirectorId() {
        return nextDirectorId.getAndIncrement();
    }

    public long nextReviewId() {
        return nextReviewId.getAndIncrement();
    }

    public long nextEventId() {
        return nextEventId.getAndIncrement();
    }
}
