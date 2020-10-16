package com.meemaw.auth.organization.datasource.sql;

import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.AVATAR;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.CREATED_AT;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.DEFAULT_ROLE;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.FIELDS;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.FIELD_MAPPINGS;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.ID;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.INSERT_FIELDS;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.NAME;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.OPEN_MEMBERSHIP;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.TABLE;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.UPDATED_AT;

import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.model.CreateOrganizationParams;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.AvatarSetupDTO;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.rest.query.UpdateDTO;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.shared.sql.rest.query.SQLUpdateDTO;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.UpdateSetFirstStep;

@ApplicationScoped
@Slf4j
public class SqlOrganizationDatasource implements OrganizationDatasource {

  @Inject SqlPool sqlPool;

  @Override
  public CompletionStage<Organization> createOrganization(
      CreateOrganizationParams params, SqlTransaction transaction) {
    return transaction
        .query(createOrganizationQuery(params))
        .thenApply(pgRowSet -> mapOrganization(pgRowSet.iterator().next()));
  }

  @Override
  public CompletionStage<Optional<Organization>> updateOrganization(
      String organizationId, UpdateDTO update) {
    UpdateSetFirstStep<?> updateStep = sqlPool.getContext().update(TABLE);
    Query query =
        SQLUpdateDTO.of(update)
            .apply(updateStep, FIELD_MAPPINGS)
            .where(ID.eq(organizationId))
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(this::mapOrganizationIfPresent);
  }

  @Override
  public CompletionStage<Organization> createOrganization(CreateOrganizationParams params) {
    return sqlPool
        .execute(createOrganizationQuery(params))
        .thenApply(pgRowSet -> mapOrganization(pgRowSet.iterator().next()));
  }

  private Query createOrganizationQuery(CreateOrganizationParams params) {
    return sqlPool
        .getContext()
        .insertInto(TABLE)
        .columns(INSERT_FIELDS)
        .values(params.getId(), params.getName())
        .returning(FIELDS);
  }

  @Override
  public CompletionStage<Optional<Organization>> findOrganization(String id) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ID.eq(id));
    return sqlPool.execute(query).thenApply(this::mapOrganizationIfPresent);
  }

  @Override
  public CompletionStage<Optional<Organization>> findOrganization(
      String id, SqlTransaction transaction) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ID.eq(id));
    return transaction.query(query).thenApply(this::mapOrganizationIfPresent);
  }

  @Override
  public CompletionStage<Boolean> delete(String id, SqlTransaction transaction) {
    Query query = sqlPool.getContext().deleteFrom(TABLE).where(ID.eq(id)).returning(ID);
    return transaction.query(query).thenApply(rows -> rows.iterator().hasNext());
  }

  @Override
  public CompletionStage<SqlTransaction> transaction() {
    return sqlPool.beginTransaction();
  }

  private Optional<Organization> mapOrganizationIfPresent(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapOrganization(rows.iterator().next()));
  }

  public static OrganizationDTO mapOrganization(Row row) {
    JsonObject avatar = (JsonObject) row.getValue(AVATAR.getName());
    return new OrganizationDTO(
        row.getString(ID.getName()),
        row.getString(NAME.getName()),
        row.getBoolean(OPEN_MEMBERSHIP.getName()),
        UserRole.fromString(row.getString(DEFAULT_ROLE.getName())),
        Optional.ofNullable(avatar).map(p -> p.mapTo(AvatarSetupDTO.class)).orElse(null),
        row.getOffsetDateTime(CREATED_AT.getName()),
        row.getOffsetDateTime(UPDATED_AT.getName()));
  }
}
