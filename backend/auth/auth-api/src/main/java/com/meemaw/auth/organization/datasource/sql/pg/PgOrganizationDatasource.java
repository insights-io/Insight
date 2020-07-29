package com.meemaw.auth.organization.datasource.sql.pg;

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
import com.meemaw.shared.sql.SQLContext;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Transaction;
import io.vertx.axle.sqlclient.Tuple;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;
import org.jooq.conf.ParamType;

@ApplicationScoped
@Slf4j
public class PgOrganizationDatasource implements OrganizationDatasource {

  @Inject PgPool pgPool;

  @Override
  @Traced
  public CompletionStage<Organization> createOrganization(
      String id, String company, Transaction transaction) {
    Query query =
        SQLContext.POSTGRES
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(id, company)
            .returning(FIELDS);

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
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
    Query query = SQLContext.POSTGRES.selectFrom(TABLE).where(ID.eq(id));
    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(this::mapOrganizationIfPresent);
  }

  @Override
  @Traced
  public CompletionStage<Optional<Organization>> findOrganization(
      String id, Transaction transaction) {
    Query query = SQLContext.POSTGRES.selectFrom(TABLE).where(ID.eq(id));
    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(this::mapOrganizationIfPresent);
  }

  private Optional<Organization> mapOrganizationIfPresent(RowSet<Row> pgRowSet) {
    if (!pgRowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapOrganization(pgRowSet.iterator().next()));
  }

  public static OrganizationDTO mapOrganization(Row row) {
    return new OrganizationDTO(
        row.getString(ID.getName()),
        row.getString(NAME.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
