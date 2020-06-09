package com.meemaw.auth.organization.datasource;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.shared.rest.exception.DatabaseException;
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

@ApplicationScoped
@Slf4j
public class PgOrganizationDatasource implements OrganizationDatasource {

  @Inject PgPool pgPool;

  private static final String CREATE_ORGANIZATION_RAW_SQL =
      "INSERT INTO auth.organization(id, name) VALUES($1, $2) RETURNING created_at";

  private static final String FIND_ORGANIZATION_RAW_SQL =
      "SELECT * FROM auth.organization WHERE id = $1";

  @Override
  public CompletionStage<Organization> createOrganization(
      String organizationId, String company, Transaction transaction) {
    return transaction
        .preparedQuery(CREATE_ORGANIZATION_RAW_SQL)
        .execute(Tuple.of(organizationId, company))
        .exceptionally(
            throwable -> {
              log.error("Failed to create organization", throwable);
              throw new DatabaseException(throwable);
            })
        .thenApply(
            pgRowSet -> {
              Row row = pgRowSet.iterator().next();
              return new OrganizationDTO(
                  organizationId, company, row.getOffsetDateTime("created_at"));
            });
  }

  @Override
  public CompletionStage<Optional<Organization>> findOrganization(String organizationId) {
    return pgPool
        .preparedQuery(FIND_ORGANIZATION_RAW_SQL)
        .execute(Tuple.of(organizationId))
        .thenApply(this::onFindOrganization);
  }

  @Override
  public CompletionStage<Optional<Organization>> findOrganization(
      String organizationId, Transaction transaction) {
    return transaction
        .preparedQuery(FIND_ORGANIZATION_RAW_SQL)
        .execute(Tuple.of(organizationId))
        .thenApply(this::onFindOrganization);
  }

  private Optional<Organization> onFindOrganization(RowSet<Row> pgRowSet) {
    if (!pgRowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapOrganization(pgRowSet.iterator().next()));
  }

  /**
   * Map SQL row to organization.
   *
   * @param row SQL row
   * @return mapped OrganizationDTO
   */
  public static OrganizationDTO mapOrganization(Row row) {
    return new OrganizationDTO(
        row.getString("id"), row.getString("name"), row.getOffsetDateTime("created_at"));
  }
}
