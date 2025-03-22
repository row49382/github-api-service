package com.row49382.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.row49382.exception.JsonDeserializationException;
import com.row49382.service.JsonService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JacksonService implements JsonService {
    private final ObjectMapper objectMapper;

    public JacksonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T deserialize(String input, Class<T> output) throws JsonDeserializationException {
        try {
            return this.objectMapper.readValue(input, output);
        } catch (JsonProcessingException e) {
            throw new JsonDeserializationException(
                    "Failed to deserialize input %s to class %s".formatted(input, output), e);
        }
    }

    @Override
    public <T> List<T> deserializeList(String input, Class<T> output) throws JsonDeserializationException {
        try {
            return this.objectMapper.readValue(input, this.objectMapper.getTypeFactory().constructCollectionType(List.class, output));
        } catch (JsonProcessingException e) {
            throw new JsonDeserializationException(
                    "Failed to deserialize input %s to list output".formatted(input), e);
        }
    }
}
