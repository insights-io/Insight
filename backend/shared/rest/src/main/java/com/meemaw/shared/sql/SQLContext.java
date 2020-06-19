package com.meemaw.shared.sql;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.ParamCastMode;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

public final class SQLContext {

  private SQLContext() {}

  public static final DSLContext POSTGRES =
      DSL.using(
          SQLDialect.POSTGRES,
          new Settings().withRenderNamedParamPrefix("$").withParamCastMode(ParamCastMode.NEVER));
}
