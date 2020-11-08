package com.meemaw.auth.sso.token.datasource.sql;

import static com.meemaw.auth.sso.token.datasource.sql.SqlAuthTokenTable.*;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import org.jooq.Query;
import org.jooq.Table;

import com.meemaw.auth.sso.token.datasource.AuthTokenDatasource;
import com.meemaw.auth.sso.token.model.CreateAuthTokenParams;
import com.meemaw.auth.sso.token.model.dto.AuthTokenDTO;
import com.meemaw.auth.user.datasource.sql.SqlUserDatasource;
import com.meemaw.auth.user.datasource.sql.SqlUserTable;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.sql.client.SqlPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SqlAuthTokenDatasource implements AuthTokenDatasource {

  @Inject SqlPool sqlPool;

  @Override
  public CompletionStage<List<AuthTokenDTO>> list(UUID userId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(USER_ID.eq(userId));
    return sqlPool.execute(query).thenApply(this::onAuthTokens);
  }

  @Override
  public CompletionStage<AuthTokenDTO> create(CreateAuthTokenParams params) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(TOKEN, USER_ID)
            .values(params.getToken(), params.getUserId())
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(rows -> mapAuthToken(rows.iterator().next()));
  }

  @Override
  public CompletionStage<Boolean> delete(String token, UUID userId) {
    Query query =
        sqlPool
            .getContext()
            .deleteFrom(TABLE)
            .where(TOKEN.eq(token).and(USER_ID.eq(userId)))
            .returning(TOKEN);

    return sqlPool.execute(query).thenApply(rows -> rows.iterator().hasNext());
  }

  @Override
  public CompletionStage<Optional<AuthUser>> getTokenUser(String token) {
    Table<?> joined = TABLE.leftJoin(SqlUserTable.TABLE).on(SqlUserTable.USER_TABLE_ID.eq(USER_ID));
    Query query = sqlPool.getContext().selectFrom(joined).where(TOKEN.eq(token));

    return sqlPool
        .execute(query)
        .thenApply(
            rows -> {
              if (!rows.iterator().hasNext()) {
                return Optional.empty();
              }
              return Optional.of(SqlUserDatasource.mapUser(rows.iterator().next()));
            });
  }

  private List<AuthTokenDTO> onAuthTokens(RowSet<Row> rows) {
    List<AuthTokenDTO> tokens = new ArrayList<>(rows.size());
    for (Row row : rows) {
      tokens.add(mapAuthToken(row));
    }
    return tokens;
  }

  public static AuthTokenDTO mapAuthToken(Row row) {
    return new AuthTokenDTO(
        row.getString(TOKEN.getName()),
        row.getUUID(USER_ID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
