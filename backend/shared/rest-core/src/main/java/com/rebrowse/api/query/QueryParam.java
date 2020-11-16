package com.rebrowse.api.query;

import lombok.Value;

@Value
public class QueryParam<T> {

  TermCondition condition;
  T value;

  public static <T> QueryParam<T> eq(T value) {
    return new QueryParam<>(TermCondition.EQ, value);
  }

  public static <T> QueryParam<T> gt(T value) {
    return new QueryParam<>(TermCondition.GT, value);
  }

  public static <T> QueryParam<T> gte(T value) {
    return new QueryParam<>(TermCondition.GTE, value);
  }

  public static <T> QueryParam<T> lt(T value) {
    return new QueryParam<>(TermCondition.LT, value);
  }

  public static <T> QueryParam<T> lte(T value) {
    return new QueryParam<>(TermCondition.LTE, value);
  }
}
