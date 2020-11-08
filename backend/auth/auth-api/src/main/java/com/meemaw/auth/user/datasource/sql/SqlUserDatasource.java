package com.meemaw.auth.user.datasource.sql;

import static com.meemaw.auth.password.datasource.sql.SqlPasswordTable.HASH;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordTable.TABLE_ALIAS;
import static com.meemaw.auth.user.datasource.sql.SqlUserTable.*;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.SelectConditionStep;
import org.jooq.UpdateSetFirstStep;
import org.jooq.impl.DSL;

import com.meemaw.auth.password.datasource.sql.SqlPasswordTable;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.UserWithLoginInformation;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.query.UpdateDTO;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.shared.sql.rest.query.SQLSearchDTO;
import com.meemaw.shared.sql.rest.query.SQLUpdateDTO;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Slf4j
public class SqlUserDatasource implements UserDatasource {

  private static final List<Field<?>> USER_WITH_LOGIN_INFORMATION_FIELDS =
      Stream.concat(
              TABLE_FIELDS.stream(),
              Stream.of(
                  SqlPasswordTable.tableAliasField(HASH),
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
      PhoneNumber phoneNumber,
      SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(email, fullName, organizationId, role.getKey(), JsonObject.mapFrom(phoneNumber))
            .returning(FIELDS);

    return transaction.execute(query).thenApply(pgRowSet -> mapUser(pgRowSet.iterator().next()));
  }

  @Override
  public CompletionStage<AuthUser> updateUser(UUID userId, UpdateDTO update) {
    UpdateSetFirstStep<?> updateStep = sqlPool.getContext().update(TABLE);
    Query query =
        SQLUpdateDTO.of(update)
            .apply(updateStep, FIELD_MAPPINGS)
            .where(ID.eq(userId))
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(rows -> mapUser(rows.iterator().next()));
  }

  @Override
  public CompletionStage<Optional<AuthUser>> findUser(String email) {
    return sqlPool.execute(findUserByEmail(email)).thenApply(this::onFindUser);
  }

  @Override
  public CompletionStage<Optional<AuthUser>> findUser(String email, SqlTransaction transaction) {
    return transaction.execute(findUserByEmail(email)).thenApply(this::onFindUser);
  }

  private Query findUserByEmail(String email) {
    return sqlPool.getContext().selectFrom(TABLE).where(EMAIL.eq(email));
  }

  @Override
  @Traced
  public CompletionStage<Optional<AuthUser>> findUser(UUID userId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ID.eq(userId));
    return sqlPool.execute(query).thenApply(this::onFindUser);
  }

  @Override
  @Traced
  public CompletionStage<Collection<AuthUser>> searchOrganizationMembers(
      String organizationId, SearchDTO search) {
    SelectConditionStep<?> searchQuery =
        searchQuery(
            sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId)),
            search);

    Query query = SQLSearchDTO.of(search).apply(searchQuery, FIELD_MAPPINGS);
    return sqlPool.execute(query).thenApply(this::onUsersFound);
  }

  @Override
  public CompletionStage<Integer> count(String organizationId, SearchDTO search) {
    SelectConditionStep<?> searchQuery =
        searchQuery(
            sqlPool
                .getContext()
                .select(DSL.count())
                .from(TABLE)
                .where(ORGANIZATION_ID.eq(organizationId)),
            search);

    Query query = SQLSearchDTO.of(search).applyFilter(searchQuery, FIELD_MAPPINGS);
    return sqlPool.execute(query).thenApply(rows -> rows.iterator().next().getInteger(0));
  }

  private SelectConditionStep<?> searchQuery(SelectConditionStep<?> baseQuery, SearchDTO search) {
    if (search.getQuery() != null) {
      return baseQuery.and(
          EMAIL
              .containsIgnoreCase(search.getQuery())
              .or(FULL_NAME.containsIgnoreCase(search.getQuery())));
    }

    return baseQuery;
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
    JsonObject phoneNumber = (JsonObject) row.getValue(PHONE_NUMBER.getName());
    return new UserDTO(
        row.getUUID(ID.getName()),
        row.getString(EMAIL.getName()),
        row.getString(FULL_NAME.getName()),
        UserRole.fromString(row.getString(ROLE.getName())),
        row.getString(ORGANIZATION_ID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()),
        row.getOffsetDateTime(UPDATED_AT.getName()),
        Optional.ofNullable(phoneNumber).map(p -> p.mapTo(PhoneNumberDTO.class)).orElse(null),
        row.getBoolean(PHONE_NUMBER_VERIFIED.getName()));
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
                TABLE
                    .leftJoin(TABLE_ALIAS)
                    .on(USER_TABLE_ID.eq(SqlPasswordTable.TABLE_ALIAS_USER_ID))
                    .leftJoin(SqlTfaSetupTable.TABLE_ALIAS)
                    .on(USER_TABLE_ID.eq(SqlTfaSetupTable.TABLE_ALIAS_USER_ID)))
            .where(EMAIL.eq(email))
            .orderBy(SqlPasswordTable.TABLE_ALIAS_CREATED_AT.desc())
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
            user.getUpdatedAt(),
            user.getPhoneNumber(),
            user.isPhoneNumberVerified(),
            password,
            tfaMethods));
  }
}
