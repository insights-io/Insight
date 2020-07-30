package com.meemaw.auth.password.datasource.sql;

import static com.meemaw.auth.password.datasource.sql.PasswordTable.CREATED_AT;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.HASH;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.TABLE;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.USER_ID;

import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.auth.user.datasource.sql.SqlUserDatasource;
import com.meemaw.auth.user.datasource.sql.UserTable;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserWithHashedPassword;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlPasswordDatasource implements PasswordDatasource {

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<Boolean> storePassword(
      UUID userId, String hashedPassword, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(USER_ID, HASH)
            .values(userId, hashedPassword);

    return transaction.query(query).thenApply(pgRowSet -> true);
  }

  @Override
  public CompletionStage<Boolean> storePassword(UUID userId, String hashedPassword) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(USER_ID, HASH)
            .values(userId, hashedPassword);

    return sqlPool.query(query).thenApply(pgRowSet -> true);
  }

  @Override
  @Traced
  public CompletionStage<Optional<UserWithHashedPassword>> findUserWithPassword(String email) {
    Query query =
        sqlPool
            .getContext()
            .selectFrom(UserTable.TABLE.leftJoin(TABLE).on(UserTable.ID.eq(USER_ID)))
            .where(UserTable.EMAIL.eq(email))
            .orderBy(PasswordTable.tableField(CREATED_AT).desc())
            .limit(1);

    return sqlPool.query(query).thenApply(this::userWithPasswordFromRowSet);
  }

  private Optional<UserWithHashedPassword> userWithPasswordFromRowSet(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapUserWithHashedPassword(rows.iterator().next()));
  }

  public static UserWithHashedPassword mapUserWithHashedPassword(Row row) {
    AuthUser user = SqlUserDatasource.mapUser(row);
    return new UserWithHashedPassword(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getRole(),
        user.getOrganizationId(),
        user.getCreatedAt(),
        row.getString(HASH.getName()));
  }
}
