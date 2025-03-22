package com.row49382.service;

import com.row49382.exception.JsonDeserializationException;

import java.util.List;

public interface JsonService {
    <T> T deserialize(String input, Class<T> output) throws JsonDeserializationException;
    <T> List<T> deserializeList(String input, Class<T> output) throws JsonDeserializationException;
}
