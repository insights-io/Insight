package com.meemaw.auth.user.datasource.sql;

import static com.meemaw.auth.user.datasource.sql.UserTable.CREATED_AT;
import static com.meemaw.auth.user.datasource.sql.UserTable.EMAIL;
import static com.meemaw.auth.user.datasource.sql.UserTable.FIELDS;
import static com.meemaw.auth.user.datasource.sql.UserTable.FULL_NAME;
import static com.meemaw.auth.user.datasource.sql.UserTable.ID;
import static com.meemaw.auth.user.datasource.sql.UserTable.INSERT_FIELDS;
import static com.meemaw.auth.user.datasource.sql.UserTable.ORGANIZATION_ID;
import static com.meemaw.auth.user.datasource.sql.UserTable.ROLE;
import static com.meemaw.auth.user.datasource.sql.UserTable.TABLE;

import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlUserDatasource implements UserDatasource {

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<AuthUser> createUser(
      String email,
      String fullName,
      String organizationId,
      UserRole role,
      SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(email, fullName, organizationId, role.toString())
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
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
