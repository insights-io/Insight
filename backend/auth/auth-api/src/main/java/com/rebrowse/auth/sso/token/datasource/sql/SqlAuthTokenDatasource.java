package com.rebrowse.auth.sso.token.datasource.sql;

import com.rebrowse.auth.sso.token.datasource.AuthTokenDatasource;
import com.rebrowse.auth.sso.token.model.CreateAuthTokenParams;
import com.rebrowse.auth.sso.token.model.dto.AuthTokenDTO;
import com.rebrowse.auth.user.datasource.sql.SqlUserDatasource;
import com.rebrowse.auth.user.datasource.sql.SqlUserTable;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.sql.client.SqlPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jooq.Query;
import org.jooq.Table;

@ApplicationScoped
public class SqlAuthTokenDatasource implements AuthTokenDatasource {

  @Inject SqlPool sqlPool;

  @Override
  public CompletionStage<List<AuthTokenDTO>> list(UUID userId) {
    Query query = sqlPool.getContext().selectFrom(SqlAuthTokenTable.TABLE).where(SqlAuthTokenTable.USER_ID.eq(userId));
    return sqlPool.execute(query).thenApply(this::onAuthTokens);
  }

  @Override
  public CompletionStage<AuthTokenDTO> create(CreateAuthTokenParams params) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(SqlAuthTokenTable.TABLE)
            .columns(SqlAuthTokenTable.TOKEN, SqlAuthTokenTable.USER_ID)
            .values(params.getToken(), params.getUserId())
            .returning(SqlAuthTokenTable.FIELDS);

    return sqlPool.execute(query).thenApply(rows -> mapAuthToken(rows.iterator().next()));
  }

  @Override
  public CompletionStage<Boolean> delete(String token, UUID userId) {
    Query query =
        sqlPool
            .getContext()
            .deleteFrom(SqlAuthTokenTable.TABLE)
            .where(SqlAuthTokenTable.TOKEN.eq(token).and(SqlAuthTokenTable.USER_ID.eq(userId)))
            .returning(SqlAuthTokenTable.TOKEN);

    return sqlPool.execute(query).thenApply(rows -> rows.iterator().hasNext());
  }

  @Override
  public CompletionStage<Optional<AuthUser>> getTokenUser(String token) {
    Table<?> joined = SqlAuthTokenTable.TABLE.leftJoin(SqlUserTable.TABLE).on(SqlUserTable.USER_TABLE_ID.eq(SqlAuthTokenTable.USER_ID));
    Query query = sqlPool.getContext().selectFrom(joined).where(SqlAuthTokenTable.TOKEN.eq(token));

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
        row.getString(SqlAuthTokenTable.TOKEN.getName()),
        row.getUUID(SqlAuthTokenTable.USER_ID.getName()),
        row.getOffsetDateTime(SqlAuthTokenTable.CREATED_AT.getName()));
  }
}
