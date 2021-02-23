package com.rebrowse.shared.sql;

import io.vertx.core.json.JsonObject;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.conf.ParamCastMode;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public final class SQLContext {

  public static final DSLContext POSTGRES =
      DSL.using(
          SQLDialect.POSTGRES,
          new Settings().withRenderNamedParamPrefix("$").withParamCastMode(ParamCastMode.NEVER));

  public static final DataType<JsonObject> JSON_OBJECT_DATA_TYPE =
      SQLDataType.VARCHAR.asConvertedDataType(new JsonObjectBinding());

  private SQLContext() {}
}
