package com.meemaw.shared.sql.rest.query;

import org.jooq.Field;
import org.jooq.impl.DSL;

public final class DatetimeFunctions {

  private DatetimeFunctions() {}

  public static <T> Field<T> dateTrunc(String datePart, Field<T> field) {
    return DSL.field("date_trunc({0}, {1})", field.getDataType(), DSL.inline(datePart), field);
  }
}
