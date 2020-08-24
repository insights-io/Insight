package com.meemaw.auth.user.datasource.sql;

import static com.meemaw.auth.password.datasource.sql.PasswordTable.HASH;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.TABLE_ALIAS;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.CREATED_AT;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.EMAIL;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.FIELDS;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.FULL_NAME;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.ID;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.INSERT_FIELDS;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.ORGANIZATION_ID;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.PHONE_NUMBER;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.ROLE;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.TABLE;

import com.meemaw.auth.password.datasource.sql.PasswordTable;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.UserWithLoginInformation;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.Collection;
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
public class SqlUserDatasource implements UserDatasource {

  private static final List<Field<?>> USER_WITH_LOGIN_INFORMATION_FIELDS =
      Stream.concat(
              SqlUserTable.TABLE_FIELDS.stream(),
              Stream.of(
                  PasswordTable.tableAliasField(HASH),
                  SqlTfaSetupTable.tableAliasField(SqlTfaSetupTable.PARAMS),
                  SqlTfaSetupTable.tableAliasField(SqlTfaSetupTable.METHOD),
                  SqlTfaSetupTable.tableAliasField(SqlTfaSetupTable.CREATED_AT)))
          .collect(Collectors.toUnmodifiableList());

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<AuthUser> createUser(
      String email,
      String fullName,
      String organizationId,
      UserRole role,
      String phoneNumber,
      SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(email, fullName, organizationId, role.toString(), phoneNumber)
            .returning(FIELDS);

    return transaction.query(query).thenApply(pgRowSet -> mapUser(pgRowSet.iterator().next()));
  }

  @Override
  @Traced
  public CompletionStage<Optional<AuthUser>> findUser(String email) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(EMAIL.eq(email));
    return sqlPool.execute(query).thenApply(this::onFindUser);
  }

  @Override
  @Traced
  public CompletionStage<Optional<AuthUser>> findUser(UUID userId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ID.eq(userId));
    return sqlPool.execute(query).thenApply(this::onFindUser);
  }

  @Override
  @Traced
  public CompletionStage<Collection<AuthUser>> findOrganizationMembers(String organizationId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
    return sqlPool.execute(query).thenApply(this::onUsersFound);
  }

  private Optional<AuthUser> onFindUser(RowSet<Row> pgRowSet) {
    if (!pgRowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    Row row = pgRowSet.iterator().next();
    return Optional.of(mapUser(row));
  }

  private Collection<AuthUser> onUsersFound(RowSet<Row> rows) {
    Collection<AuthUser> users = new ArrayList<>();
    rows.forEach(row -> users.add(mapUser(row)));
    return users;
  }

  public static AuthUser mapUser(Row row) {
    return new UserDTO(
        row.getUUID(ID.getName()),
        row.getString(EMAIL.getName()),
        row.getString(FULL_NAME.getName()),
        UserRole.valueOf(row.getString(ROLE.getName())),
        row.getString(ORGANIZATION_ID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()),
        row.getString(PHONE_NUMBER.getName()));
  }

  @Override
  @Traced
  public CompletionStage<Optional<UserWithLoginInformation>> findUserWithLoginInformation(
      String email) {
    Query query =
        sqlPool
            .getContext()
            .select(USER_WITH_LOGIN_INFORMATION_FIELDS)
            .from(
                SqlUserTable.TABLE
                    .leftJoin(TABLE_ALIAS)
                    .on(SqlUserTable.USER_TABLE_ID.eq(PasswordTable.TABLE_ALIAS_USER_ID))
                    .leftJoin(SqlTfaSetupTable.TABLE_ALIAS)
                    .on(SqlUserTable.USER_TABLE_ID.eq(SqlTfaSetupTable.TABLE_ALIAS_USER_ID)))
            .where(SqlUserTable.EMAIL.eq(email))
            .orderBy(PasswordTable.TABLE_ALIAS_CREATED_AT.desc())
            .limit(2);

    return sqlPool.execute(query).thenApply(this::userWithLoginInformationFromRowSet);
  }

  private Optional<UserWithLoginInformation> userWithLoginInformationFromRowSet(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }

    Row firstRow = rows.iterator().next();
    AuthUser user = SqlUserDatasource.mapUser(firstRow);
    String password = firstRow.getString(HASH.getName());

    List<TfaMethod> tfaMethods = new ArrayList<>(rows.size());
    for (Row row : rows) {
      Optional.ofNullable(row.getString(SqlTfaSetupTable.METHOD.getName()))
          .ifPresent(method -> tfaMethods.add(TfaMethod.fromString(method)));
    }

    return Optional.of(
        new UserWithLoginInformation(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole(),
            user.getOrganizationId(),
            user.getCreatedAt(),
            user.getPhoneNumber(),
            password,
            tfaMethods));
  }
}
