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
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DataStore;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final DataStore store;

    @GetMapping
    public Collection<User> findAll() {
        return store.users.values();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return getOrThrow(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        applyNameFallback(user);
        user.setId(store.nextUserId());
        store.users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!store.users.containsKey(user.getId())) {
            log.warn("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        user.getFriends().addAll(store.users.get(user.getId()).getFriends());
        applyNameFallback(user);
        store.users.put(user.getId(), user);
        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        getOrThrow(id);
        for (User other : store.users.values()) {
            other.getFriends().remove(id);
        }
        store.users.remove(id);
        log.info("Удалён пользователь id={}", id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        User user = getOrThrow(id);
        User friend = getOrThrow(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);
        store.events.add(new Event(store.nextEventId(), id, System.currentTimeMillis(), "FRIEND", "ADD", friendId));
        log.info("Пользователь {} добавил в друзья {}", id, friendId);
        return user;
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        User user = getOrThrow(id);
        User friend = getOrThrow(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
        store.events.add(new Event(store.nextEventId(), id, System.currentTimeMillis(), "FRIEND", "REMOVE", friendId));
        log.info("Пользователь {} удалил из друзей {}", id, friendId);
        return user;
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        User user = getOrThrow(id);
        return user.getFriends().stream()
                .filter(store.users::containsKey)
                .map(store.users::get)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        User user = getOrThrow(id);
        User other = getOrThrow(otherId);
        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .filter(store.users::containsKey)
                .map(store.users::get)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/feed")
    public List<Event> getFeed(@PathVariable Long id) {
        getOrThrow(id);
        return store.events.stream()
                .filter(e -> e.getUserId().equals(id))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable Long id) {
        getOrThrow(id);
        Set<Long> myLikes = likedFilmIds(id);

        long bestUserId = -1;
        int bestIntersection = 0;
        for (Long otherId : store.users.keySet()) {
            if (otherId.equals(id)) continue;
            Set<Long> otherLikes = likedFilmIds(otherId);
            long intersection = myLikes.stream().filter(otherLikes::contains).count();
            if (intersection > bestIntersection) {
                bestIntersection = (int) intersection;
                bestUserId = otherId;
            }
        }
        if (bestUserId == -1) {
            return List.of();
        }
        final long similarUserId = bestUserId;
        return likedFilmIds(similarUserId).stream()
                .filter(filmId -> !myLikes.contains(filmId))
                .map(store.films::get)
                .filter(f -> f != null)
                .collect(Collectors.toList());
    }

    private Set<Long> likedFilmIds(Long userId) {
        return store.films.entrySet().stream()
                .filter(e -> e.getValue().getLikes().contains(userId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private User getOrThrow(Long id) {
        User user = store.users.get(id);
        if (user == null) {
            log.warn("Пользователь с id={} не найден", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user;
    }

    private void applyNameFallback(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
