package com.row49382.service;

@FunctionalInterface
public interface JsonDeserializationStrategy<T> {
    T deserialize(String input);
}
