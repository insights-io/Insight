package com.rebrowse.session.sessions.resource.v1;

import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.session.sessions.datasource.SessionDatasource;
import com.rebrowse.session.sessions.datasource.SessionTable;
import com.rebrowse.session.sessions.service.SessionService;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.query.AbstractQueryParser;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionResourceImpl implements SessionResource {

  @Context HttpServletRequest request;
  @Context UriInfo uriInfo;
  @Inject AuthPrincipal principal;
  @Inject SessionService sessionService;
  @Inject SessionDatasource sessionDatasource;

  @Override
  public CompletionStage<Response> retrieve(UUID sessionId) {
    String organizationId = principal.user().getOrganizationId();
    return sessionService
        .retrieveSession(sessionId, organizationId)
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> count() {
    String organizationId = principal.user().getOrganizationId();
    SearchDTO search =
        SearchDTO.withAllowedFields(SessionTable.QUERYABLE_FIELDS)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    return sessionDatasource.count(organizationId, search).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> distinct(List<String> on) {
    Map<String, String> errors = new HashMap<>();
    List<String> snakeCaseFields =
        on.stream().map(AbstractQueryParser::snakeCase).collect(Collectors.toList());

    for (String field : snakeCaseFields) {
      if (!SessionTable.QUERYABLE_FIELDS.contains(field)) {
        errors.put(field, "Unexpected field");
      }
    }

    if (!errors.isEmpty()) {
      return CompletableFuture.completedStage(Boom.badRequest().errors(errors).response());
    }

    String organizationId = principal.user().getOrganizationId();
    Map<String, List<String>> params = RequestUtils.map(uriInfo.getQueryParameters());
    params.remove("on");
    SearchDTO search = SearchDTO.withAllowedFields(SessionTable.QUERYABLE_FIELDS).rhsColon(params);
    return sessionDatasource
        .distinct(snakeCaseFields, organizationId, search)
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> list() {
    String organizationId = principal.user().getOrganizationId();
    SearchDTO searchDTO =
        SearchDTO.withAllowedFields(SessionTable.QUERYABLE_FIELDS)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    return sessionService.listSessions(organizationId, searchDTO).thenApply(DataResponse::ok);
  }
}
