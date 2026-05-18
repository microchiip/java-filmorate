package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.Instant;

@Data
public class Event {
    private Long eventId;
    private Long userId;
    private Long entityId;
    private String eventType;  // LIKE, REVIEW, FRIEND
    private String operation;   // ADD, UPDATE, REMOVE
    private Long timestamp;
}