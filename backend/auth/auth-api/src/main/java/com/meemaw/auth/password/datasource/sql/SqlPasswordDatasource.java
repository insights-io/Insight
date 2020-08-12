package com.meemaw.auth.password.datasource.sql;

import static com.meemaw.auth.password.datasource.sql.PasswordTable.HASH;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.TABLE;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.TABLE_ALIAS;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.USER_ID;

import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.auth.user.datasource.sql.SqlUserDatasource;
import com.meemaw.auth.user.datasource.sql.TFASetupTable;
import com.meemaw.auth.user.datasource.sql.UserTable;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserWithLoginInformation;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Field;
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

    return sqlPool.execute(query).thenApply(pgRowSet -> true);
  }

  private static final List<Field<?>> USER_WITH_LOGIN_INFORMATION_FIELDS =
      Stream.concat(
              UserTable.TABLE_FIELDS.stream(),
              Stream.of(
                  PasswordTable.tableAliasField(HASH),
                  TFASetupTable.tableAliasField(TFASetupTable.SECRET)))
          .collect(Collectors.toUnmodifiableList());

  @Override
  @Traced
  public CompletionStage<Optional<UserWithLoginInformation>> findUserWithLoginInformation(
      String email) {

    Query query =
        sqlPool
            .getContext()
            .select(USER_WITH_LOGIN_INFORMATION_FIELDS)
            .from(
                UserTable.TABLE
                    .leftJoin(TABLE_ALIAS)
                    .on(UserTable.USER_TABLE_ID.eq(PasswordTable.TABLE_ALIAS_USER_ID))
                    .leftJoin(TFASetupTable.TABLE_ALIAS)
                    .on(UserTable.USER_TABLE_ID.eq(TFASetupTable.TABLE_ALIAS_USER_ID)))
            .where(UserTable.EMAIL.eq(email))
            .orderBy(PasswordTable.TABLE_ALIAS_CREATED_AT.desc())
            .limit(1);

    return sqlPool.execute(query).thenApply(this::userWithLoginInformationFromRowSet);
  }

  private Optional<UserWithLoginInformation> userWithLoginInformationFromRowSet(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapUserWithHashedPassword(rows.iterator().next()));
  }

  public static UserWithLoginInformation mapUserWithHashedPassword(Row row) {
    AuthUser user = SqlUserDatasource.mapUser(row);
    boolean tfaConfigured = row.getString(TFASetupTable.SECRET.getName()) != null;

    return new UserWithLoginInformation(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getRole(),
        user.getOrganizationId(),
        user.getCreatedAt(),
        row.getString(HASH.getName()),
        tfaConfigured);
  }
}
