package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Event {
    private Long eventId;
    private Long userId;
    private long timestamp;
    private String eventType;
    private String operation;
    private Long entityId;
}
