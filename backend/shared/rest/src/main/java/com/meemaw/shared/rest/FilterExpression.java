package com.meemaw.shared.rest;

import java.util.List;
import lombok.Data;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;

@Data
public abstract class FilterExpression {

  List<FilterExpression> children;

  public abstract SelectConditionStep<?> sql(SelectJoinStep<?> query);
}
