package com.meemaw.shared.rest.query;

import lombok.Value;

@Value
public class TermFilterExpression<T> implements FilterExpression {

  String field;
  TermOperation operation;
  T target;
}
