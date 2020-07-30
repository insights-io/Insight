package com.meemaw.auth.organization.datasource.sql;

import static com.meemaw.auth.organization.datasource.sql.OrganizationTable.CREATED_AT;
import static com.meemaw.auth.organization.datasource.sql.OrganizationTable.FIELDS;
import static com.meemaw.auth.organization.datasource.sql.OrganizationTable.ID;
import static com.meemaw.auth.organization.datasource.sql.OrganizationTable.INSERT_FIELDS;
import static com.meemaw.auth.organization.datasource.sql.OrganizationTable.NAME;
import static com.meemaw.auth.organization.datasource.sql.OrganizationTable.TABLE;

import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlOrganizationDatasource implements OrganizationDatasource {

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<Organization> createOrganization(
      String id, String company, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(id, company)
            .returning(FIELDS);

    return transaction
        .query(query)
        .exceptionally(
            throwable -> {
              log.error("Failed to create organization", throwable);
              throw new DatabaseException(throwable);
            })
        .thenApply(pgRowSet -> mapOrganization(pgRowSet.iterator().next()));
  }

  @Override
  @Traced
  public CompletionStage<Optional<Organization>> findOrganization(String id) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ID.eq(id));
    return sqlPool.query(query).thenApply(this::mapOrganizationIfPresent);
  }

  @Override
  @Traced
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
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
