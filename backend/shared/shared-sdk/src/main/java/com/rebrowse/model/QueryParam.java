package com.rebrowse.model;

import lombok.Value;

@Value
public class QueryParam<T> {

  TermOperation operation;
  T value;

  public static <T> QueryParam<T> eq(T value) {
    return new QueryParam<>(TermOperation.EQ, value);
  }

  public static <T> QueryParam<T> gt(T value) {
    return new QueryParam<>(TermOperation.GT, value);
  }

  public static <T> QueryParam<T> gte(T value) {
    return new QueryParam<>(TermOperation.GTE, value);
  }

  public static <T> QueryParam<T> lt(T value) {
    return new QueryParam<>(TermOperation.LT, value);
  }

  public static <T> QueryParam<T> lte(T value) {
    return new QueryParam<>(TermOperation.LTE, value);
  }
}
