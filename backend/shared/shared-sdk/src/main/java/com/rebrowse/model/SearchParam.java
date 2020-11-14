package com.rebrowse.model;

import lombok.Value;

@Value
public class SearchParam<T> {

  TermOperation operation;
  T value;

  public static <T> SearchParam<T> eq(T value) {
    return new SearchParam<>(TermOperation.EQ, value);
  }

  public static <T> SearchParam<T> gt(T value) {
    return new SearchParam<>(TermOperation.GT, value);
  }

  public static <T> SearchParam<T> gte(T value) {
    return new SearchParam<>(TermOperation.GTE, value);
  }

  public static <T> SearchParam<T> lt(T value) {
    return new SearchParam<>(TermOperation.LT, value);
  }

  public static <T> SearchParam<T> lte(T value) {
    return new SearchParam<>(TermOperation.LTE, value);
  }
}
