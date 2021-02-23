package com.rebrowse.auth.organization.resource.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.auth.organization.datasource.OrganizationPasswordPolicyDatasource;
import com.rebrowse.auth.organization.datasource.OrganizationPasswordPolicyTable;
import com.rebrowse.auth.organization.model.dto.PasswordPolicyDTO;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.shared.rest.query.UpdateDTO;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.core.Response;

public class OrganizationPasswordPolicyResourceImpl implements OrganizationPasswordPolicyResource {

  @Inject ObjectMapper objectMapper;
  @Inject AuthPrincipal authPrincipal;
  @Inject
  OrganizationPasswordPolicyDatasource passwordPolicyDatasource;
  @Inject Validator validator;

  @Override
  public CompletionStage<Response> retrieve() {
    String organizationId = authPrincipal.user().getOrganizationId();
    return passwordPolicyDatasource
        .retrieve(organizationId)
        .thenApply(
            maybePolicy ->
                DataResponse.ok(maybePolicy.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> create(Map<String, Object> body) {
    UpdateDTO update = validateBody(body);
    String organizationId = authPrincipal.user().getOrganizationId();
    return passwordPolicyDatasource
        .create(organizationId, update.getParams())
        .thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> update(Map<String, Object> body) {
    UpdateDTO update = validateBody(body);
    String organizationId = authPrincipal.user().getOrganizationId();
    return passwordPolicyDatasource
        .update(organizationId, update)
        .thenApply(
            maybePolicy ->
                DataResponse.ok(maybePolicy.orElseThrow(() -> Boom.notFound().exception())));
  }

  private UpdateDTO validateBody(Map<String, Object> body) {
    if (body.isEmpty()) {
      throw Boom.badRequest()
          .message("Validation Error")
          .errors(Map.of("body", "Required"))
          .exception();
    }

    PasswordPolicyDTO passwordPolicy = objectMapper.convertValue(body, PasswordPolicyDTO.class);
    Set<ConstraintViolation<PasswordPolicyDTO>> constraintViolations =
        validator.validate(passwordPolicy);
    if (!constraintViolations.isEmpty()) {
      throw new ConstraintViolationException(constraintViolations);
    }

    UpdateDTO update = UpdateDTO.from(body);
    Map<String, String> errors = update.validate(OrganizationPasswordPolicyTable.UPDATABLE_FIELDS);
    if (!errors.isEmpty()) {
      throw Boom.badRequest().errors(errors).exception();
    }

    return update;
  }
}
