package com.meemaw.auth.organization.datasource;

import com.meemaw.auth.organization.model.dto.PasswordPolicyDTO;
import com.meemaw.shared.rest.query.UpdateDTO;
import com.meemaw.shared.sql.client.SqlTransaction;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface OrganizationPasswordPolicyDatasource {

  CompletionStage<Optional<PasswordPolicyDTO>> retrieve(String organizationId);

  CompletionStage<Optional<PasswordPolicyDTO>> retrieve(
      String organizationId, SqlTransaction transaction);

  CompletionStage<PasswordPolicyDTO> create(String organizationId, Map<String, Object> params);

  CompletionStage<Optional<PasswordPolicyDTO>> update(String organizationId, UpdateDTO update);
}
