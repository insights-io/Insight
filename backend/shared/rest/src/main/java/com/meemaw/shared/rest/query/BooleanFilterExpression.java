package com.meemaw.shared.rest.query;

import lombok.Value;

import java.util.List;

@Value
public class BooleanFilterExpression<T extends FilterExpression> implements FilterExpression {

  BooleanOperation operation;
  List<T> children;
}
