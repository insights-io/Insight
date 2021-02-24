package com.rebrowse.shared.rest.query;

import java.util.List;
import lombok.Value;

@Value
public class BooleanFilterExpression<T extends FilterExpression> implements FilterExpression {

  BooleanOperation operation;
  List<T> children;
}
