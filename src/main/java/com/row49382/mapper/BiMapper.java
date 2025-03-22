package com.row49382.mapper;

/**
 * Takes two inputs and merges them to one output
 * @param <T> The first input
 * @param <U> The second input
 * @param <V> The output
 */
@FunctionalInterface
public interface BiMapper<T,U,V> {
    V map(T firstInput, U secondInput);
}
