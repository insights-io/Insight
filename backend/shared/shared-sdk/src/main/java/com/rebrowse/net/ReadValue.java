package com.rebrowse.net;

import com.fasterxml.jackson.core.JsonProcessingException;

@FunctionalInterface
public interface ReadValue<T, R> {

  R apply(T t) throws JsonProcessingException;
}
