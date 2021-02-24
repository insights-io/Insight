package com.rebrowse.auth.organization.datasource.sql;

import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.CREATED_AT;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.FIELDS;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.FIELD_MAPPINGS;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.MIN_CHARACTERS;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.ORGANIZATION_ID;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.PREVENT_PASSWORD_REUSE;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.REQUIRE_LOWERCASE_CHARACTER;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.REQUIRE_NON_ALPHANUMERIC_CHARACTER;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.REQUIRE_NUMBER;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.REQUIRE_UPPERCASE_CHARACTER;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.TABLE;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationPasswordPolicyTable.UPDATED_AT;

import com.google.common.base.CaseFormat;
import com.rebrowse.auth.organization.datasource.OrganizationPasswordPolicyDatasource;
import com.rebrowse.auth.organization.model.dto.PasswordPolicyDTO;
import com.rebrowse.shared.rest.query.UpdateDTO;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.shared.sql.datasource.AbstractSqlDatasource;
import com.rebrowse.shared.sql.rest.query.SQLUpdateDTO;
import io.vertx.mutiny.sqlclient.Row;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jooq.Query;
import org.jooq.UpdateSetFirstStep;

@ApplicationScoped
public class SqlOrganizationPasswordPolicyDatasource
    extends AbstractSqlDatasource<PasswordPolicyDTO>
    implements OrganizationPasswordPolicyDatasource {

  @Inject SqlPool sqlPool;

  public static PasswordPolicyDTO map(Row row) {
    return new PasswordPolicyDTO(
        row.getString(ORGANIZATION_ID.getName()),
        row.getShort(MIN_CHARACTERS.getName()),
        row.getBoolean(PREVENT_PASSWORD_REUSE.getName()),
        row.getBoolean(REQUIRE_UPPERCASE_CHARACTER.getName()),
        row.getBoolean(REQUIRE_LOWERCASE_CHARACTER.getName()),
        row.getBoolean(REQUIRE_NUMBER.getName()),
        row.getBoolean(REQUIRE_NON_ALPHANUMERIC_CHARACTER.getName()),
        row.getOffsetDateTime(UPDATED_AT.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }

  @Override
  public PasswordPolicyDTO fromSql(Row row) {
    return SqlOrganizationPasswordPolicyDatasource.map(row);
  }

  private Query retrieveQuery(String organizationId) {
    return sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
  }

  @Override
  public CompletionStage<Optional<PasswordPolicyDTO>> retrieve(String organizationId) {
    return sqlPool.execute(retrieveQuery(organizationId)).thenApply(this::findOne);
  }

  @Override
  public CompletionStage<Optional<PasswordPolicyDTO>> retrieve(
      String organizationId, SqlTransaction transaction) {
    return transaction.execute(retrieveQuery(organizationId)).thenApply(this::findOne);
  }

  @Override
  public CompletionStage<PasswordPolicyDTO> create(
      String organizationId, Map<String, Object> params) {
    List<String> columns =
        params.keySet().stream()
            .map(f -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, f))
            .collect(Collectors.toList());
    columns.add(ORGANIZATION_ID.getName());

    List<Object> values = new ArrayList<>(params.values());
    values.add(organizationId);

    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(columns.stream().map(FIELD_MAPPINGS::get).collect(Collectors.toList()))
            .values(values)
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(this::expectOne);
  }

  @Override
  public CompletionStage<Optional<PasswordPolicyDTO>> update(
      String organizationId, UpdateDTO update) {
    UpdateSetFirstStep<?> updateStep = sqlPool.getContext().update(TABLE);
    Query query =
        SQLUpdateDTO.of(update)
            .apply(updateStep, FIELD_MAPPINGS)
            .where(ORGANIZATION_ID.eq(organizationId))
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(this::findOne);
  }
}
