package com.meemaw.auth.user.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.user.datasource.UserTable;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.service.UserService;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class UserResourceImpl implements UserResource {

  @Inject InsightPrincipal principal;
  @Inject UserService userService;

  @Override
  public CompletionStage<Response> update(Map<String, ?> body) {
    if (body.isEmpty()) {
      return CompletableFuture.completedStage(
          Boom.badRequest()
              .message("Validation Error")
              .errors(Map.of("body", "Required"))
              .response());
    }

    Map<String, String> errors = new HashMap<>();
    for (Entry<String, ?> entry : body.entrySet()) {
      if (!UserTable.UPDATABLE_FIELDS.contains(entry.getKey())) {
        errors.put(entry.getKey(), "Unexpected field");
      }
    }

    if (errors.size() > 0) {
      return CompletableFuture.completedStage(Boom.badRequest().errors(errors).response());
    }

    AuthUser user = principal.user();
    return userService.updateUser(user.getId(), body).thenApply(DataResponse::ok);
  }
}
