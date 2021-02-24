package com.rebrowse.shared.sql;

import io.vertx.core.json.JsonObject;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;
import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;

public class JsonObjectBinding implements Binding<Object, JsonObject> {

  private static final Converter<Object, JsonObject> CONVERTER =
      new Converter<>() {
        @Override
        public JsonObject from(Object databaseJsonObject) {
          return (JsonObject) databaseJsonObject;
        }

        @Override
        public Object to(JsonObject vertxJsonObject) {
          return vertxJsonObject.toString();
        }

        @Override
        public Class<Object> fromType() {
          return Object.class;
        }

        @Override
        public Class<JsonObject> toType() {
          return JsonObject.class;
        }
      };

  @Override
  public Converter<Object, JsonObject> converter() {
    return CONVERTER;
  }

  // Rending a bind variable for the binding context's value and casting it to the json type
  @Override
  public void sql(BindingSQLContext<JsonObject> ctx) {
    ctx.render().sql(ctx.variable());
  }

  // Registering VARCHAR types for JDBC CallableStatement OUT parameters
  @Override
  public void register(BindingRegisterContext<JsonObject> ctx) throws SQLException {
    ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
  }

  // Converting the JsonObject to a String value and setting that on a JDBC PreparedStatement
  @Override
  public void set(BindingSetStatementContext<JsonObject> ctx) throws SQLException {
    ctx.statement()
        .setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
  }

  // Setting a value on a JDBC SQLOutput (useful for Oracle OBJECT types)
  @Override
  public void set(BindingSetSQLOutputContext<JsonObject> ctx) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  // Getting a String value from a JDBC ResultSet and converting that to a JsonObject
  @Override
  public void get(BindingGetResultSetContext<JsonObject> ctx) throws SQLException {
    ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
  }

  // Getting a String value from a JDBC CallableStatement and converting that to a JsonObject
  @Override
  public void get(BindingGetStatementContext<JsonObject> ctx) throws SQLException {
    ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
  }

  // Getting a value from a JDBC SQLInput (useful for Oracle OBJECT types)
  @Override
  public void get(BindingGetSQLInputContext<JsonObject> ctx) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }
}
