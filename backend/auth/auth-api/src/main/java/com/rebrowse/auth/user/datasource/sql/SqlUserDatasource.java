package com.rebrowse.auth.user.datasource.sql;

import static com.rebrowse.auth.password.datasource.sql.SqlPasswordTable.HASH;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.CREATED_AT;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.EMAIL;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.FIELDS;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.FIELD_MAPPINGS;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.FULL_NAME;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.ID;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.INSERT_FIELDS;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.ORGANIZATION_ID;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.PHONE_NUMBER;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.PHONE_NUMBER_VERIFIED;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.ROLE;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.TABLE;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.UPDATED_AT;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.USER_TABLE_ID;
import static com.rebrowse.auth.user.datasource.sql.SqlUserTable.WITH_LOGIN_INFORMATION_FIELDS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.password.datasource.sql.SqlPasswordTable;
import com.rebrowse.auth.user.datasource.UserDatasource;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.PhoneNumber;
import com.rebrowse.auth.user.model.UserRole;
import com.rebrowse.auth.user.model.UserWithLoginInformation;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.auth.user.model.dto.UserDTO;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.rest.query.UpdateDTO;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.shared.sql.datasource.AbstractSqlDatasource;
import com.rebrowse.shared.sql.rest.query.SQLGroupByQuery;
import com.rebrowse.shared.sql.rest.query.SQLSearchDTO;
import com.rebrowse.shared.sql.rest.query.SQLUpdateDTO;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.SelectConditionStep;
import org.jooq.UpdateSetFirstStep;

@ApplicationScoped
@Slf4j
public class SqlUserDatasource extends AbstractSqlDatasource<AuthUser> implements UserDatasource {

  @Inject ObjectMapper objectMapper;
  @Inject SqlPool sqlPool;

  public static UserDTO mapUser(Row row) {
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
  public CompletionStage<AuthUser> create(
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
  public CompletionStage<Boolean> exists(String email) {
    Query query = sqlPool.getContext().selectOne().from(TABLE).where(EMAIL.eq(email));
    return sqlPool.execute(query).thenApply(rows -> rows.iterator().hasNext());
  }

  @Override
  public CompletionStage<AuthUser> update(UUID userId, UpdateDTO update) {
    return sqlPool
        .execute(updateUserQuery(userId, update))
        .thenApply(rows -> mapUser(rows.iterator().next()));
  }

  @Override
  public CompletionStage<AuthUser> update(
      UUID userId, UpdateDTO update, SqlTransaction transaction) {
    return transaction
        .execute(updateUserQuery(userId, update))
        .thenApply(rows -> mapUser(rows.iterator().next()));
  }

  private Query updateUserQuery(UUID userId, UpdateDTO update) {
    UpdateSetFirstStep<?> updateStep = sqlPool.getContext().update(TABLE);
    return SQLUpdateDTO.of(update)
        .apply(updateStep, FIELD_MAPPINGS)
        .where(ID.eq(userId))
        .returning(FIELDS);
  }

  @Override
  public CompletionStage<Optional<AuthUser>> retrieve(String email) {
    return sqlPool.execute(findUserByEmail(email)).thenApply(this::findOne);
  }

  @Override
  public CompletionStage<Optional<AuthUser>> retrieve(String email, SqlTransaction transaction) {
    return transaction.execute(findUserByEmail(email)).thenApply(this::findOne);
  }

  @Override
  public CompletionStage<Optional<Pair<UserDTO, List<MfaMethod>>>> retrieveUserWithMfaMethods(
      UUID userId) {
    Query query =
        sqlPool
            .getContext()
            .select(SqlUserTable.WITH_MFA_METHODS_FIELDS)
            .from(
                SqlUserTable.TABLE
                    .leftJoin(SqlMfaConfigurationTable.TABLE_ALIAS)
                    .on(USER_TABLE_ID.eq(SqlMfaConfigurationTable.TABLE_ALIAS_USER_ID)))
            .where(ID.eq(userId))
            .limit(MfaMethod.NUM_METHODS);

    return sqlPool.execute(query).thenApply(this::userWithMfaMethods);
  }

  private Query findUserByEmail(String email) {
    return sqlPool.getContext().selectFrom(TABLE).where(EMAIL.eq(email));
  }

  @Override
  public CompletionStage<Optional<AuthUser>> retrieve(UUID userId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ID.eq(userId));
    return sqlPool.execute(query).thenApply(this::findOne);
  }

  @Override
  @Traced
  public CompletionStage<Collection<AuthUser>> searchOrganizationMembers(
      String organizationId, SearchDTO search) {
    SelectConditionStep<?> searchQuery =
        searchQuery(
            sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId)),
            search);
    Query query = SQLSearchDTO.of(search).query(searchQuery, FIELD_MAPPINGS);
    return sqlPool.execute(query).thenApply(this::findMany);
  }

  @Override
  public CompletionStage<JsonNode> count(String organizationId, SearchDTO search) {
    List<Field<?>> columns =
        SQLGroupByQuery.of(search.getGroupBy(), search.getDateTrunc()).selectFieldsWithCount();

    Query query =
        SQLSearchDTO.of(search)
            .query(
                searchQuery(
                    sqlPool
                        .getContext()
                        .select(columns)
                        .from(TABLE)
                        .where(ORGANIZATION_ID.eq(organizationId)),
                    search),
                FIELD_MAPPINGS);

    return sqlPool
        .execute(query)
        .thenApply(rows -> SQLGroupByQuery.mapRowsToJsonNode(rows, columns, objectMapper));
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

  @Override
  @Traced
  public CompletionStage<Optional<UserWithLoginInformation>> retrieveUserWithLoginInformation(
      String email) {
    Query query =
        sqlPool
            .getContext()
            .select(WITH_LOGIN_INFORMATION_FIELDS)
            .from(
                SqlUserTable.TABLE
                    .leftJoin(SqlPasswordTable.TABLE_ALIAS)
                    .on(USER_TABLE_ID.eq(SqlPasswordTable.TABLE_ALIAS_USER_ID))
                    .leftJoin(SqlMfaConfigurationTable.TABLE_ALIAS)
                    .on(USER_TABLE_ID.eq(SqlMfaConfigurationTable.TABLE_ALIAS_USER_ID)))
            .where(EMAIL.eq(email))
            .orderBy(SqlPasswordTable.TABLE_ALIAS_CREATED_AT.desc())
            .limit(MfaMethod.NUM_METHODS);

    return sqlPool.execute(query).thenApply(this::userWithLoginInformationFromRowSet);
  }

  private Optional<Pair<UserDTO, List<MfaMethod>>> userWithMfaMethods(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    Row firstRow = rows.iterator().next();
    UserDTO user = SqlUserDatasource.mapUser(firstRow);

    List<MfaMethod> mfaMethods = new ArrayList<>(rows.size());
    for (Row row : rows) {
      Optional.ofNullable(row.getString(SqlMfaConfigurationTable.METHOD.getName()))
          .ifPresent(method -> mfaMethods.add(MfaMethod.fromString(method)));
    }

    return Optional.of(Pair.of(user, mfaMethods));
  }

  private Optional<UserWithLoginInformation> userWithLoginInformationFromRowSet(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }

    Row firstRow = rows.iterator().next();
    AuthUser user = SqlUserDatasource.mapUser(firstRow);
    String password = firstRow.getString(HASH.getName());

    List<MfaMethod> mfaMethods = new ArrayList<>(rows.size());
    for (Row row : rows) {
      Optional.ofNullable(row.getString(SqlMfaConfigurationTable.METHOD.getName()))
          .ifPresent(method -> mfaMethods.add(MfaMethod.fromString(method)));
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
            mfaMethods));
  }

  @Override
  public AuthUser fromSql(Row row) {
    return SqlUserDatasource.mapUser(row);
  }
}
