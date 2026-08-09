package com.shecancode.attendence.Attendence.Event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shecancode.attendence.Attendence.Exception.EventSerializationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventSerializer {
    private final ObjectMapper objectMapper;

    public <T> String serialize(T event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException(
                    "Failed to serialize event", e
            );
        }
    }
}
