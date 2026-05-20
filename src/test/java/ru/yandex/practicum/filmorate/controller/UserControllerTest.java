package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DataStore;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(new DataStore());
    }

    private User validUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user_login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void createAssignsIdAndStoresUser() {
        User created = controller.create(validUser());
        assertNotNull(created.getId());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void emptyNameIsReplacedWithLogin() {
        User user = validUser();
        user.setName("");
        User created = controller.create(user);
        assertEquals("user_login", created.getName());
    }

    @Test
    void nullNameIsReplacedWithLogin() {
        User user = validUser();
        user.setName(null);
        User created = controller.create(user);
        assertEquals("user_login", created.getName());
    }

    @Test
    void updateWithoutIdFails() {
        User user = validUser();
        assertThrows(ValidationException.class, () -> controller.update(user));
    }

    @Test
    void updateWithUnknownIdFails() {
        User user = validUser();
        user.setId(999L);
        assertThrows(NotFoundException.class, () -> controller.update(user));
    }

    @Test
    void updateChangesStoredUser() {
        User created = controller.create(validUser());
        created.setName("New Name");
        User updated = controller.update(created);
        assertEquals("New Name", updated.getName());
    }
}
