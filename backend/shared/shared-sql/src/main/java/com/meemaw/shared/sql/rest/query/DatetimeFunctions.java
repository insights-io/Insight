package com.meemaw.shared.sql.rest.query;

import com.meemaw.shared.rest.query.TimePrecision;
import org.jooq.Field;
import org.jooq.impl.DSL;

public final class DatetimeFunctions {

  private DatetimeFunctions() {}

  public static <T> Field<T> dateTrunc(Field<T> field, TimePrecision timePrecision) {
    return DSL.field(
        "date_trunc({0}, {1})", field.getDataType(), DSL.inline(timePrecision.getKey()), field);
  }
}
