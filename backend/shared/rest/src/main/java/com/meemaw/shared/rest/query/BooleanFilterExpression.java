package com.meemaw.shared.rest.query;

import com.meemaw.shared.rest.query.sql.BooleanOperation;
import java.util.List;
import lombok.Value;

@Value
public class BooleanFilterExpression<T extends FilterExpression> implements FilterExpression {

  BooleanOperation operator;
  List<T> children;
}
