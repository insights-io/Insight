package com.meemaw.auth.organization.datasource.sql;

import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.CREATED_AT;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.FIELDS;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.ID;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.INSERT_FIELDS;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.NAME;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.PLAN;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.TABLE;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTable.UPDATED_AT;

import com.meemaw.auth.billing.model.SubscriptionPlan;
import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.model.CreateOrganizationParams;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;

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
        .values(params.getId(), params.getName(), params.getPlan())
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

  private Optional<Organization> mapOrganizationIfPresent(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapOrganization(rows.iterator().next()));
  }

  public static OrganizationDTO mapOrganization(Row row) {
    return new OrganizationDTO(
        row.getString(ID.getName()),
        row.getString(NAME.getName()),
        SubscriptionPlan.fromString(row.getString(PLAN.getName())),
        row.getOffsetDateTime(CREATED_AT.getName()),
        row.getOffsetDateTime(UPDATED_AT.getName()));
  }
}
