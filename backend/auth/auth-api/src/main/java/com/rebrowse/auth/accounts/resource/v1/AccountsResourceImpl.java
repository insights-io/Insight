package com.rebrowse.auth.accounts.resource.v1;

import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.ChooseAccountResponse;
import com.rebrowse.auth.accounts.service.AccountsService;
import com.rebrowse.shared.context.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountsResourceImpl implements AccountsResource {

  @Inject AccountsService accountsService;

  @Context HttpServerRequest request;
  @Context UriInfo info;

  @Override
  public CompletionStage<Response> choose(String email, URL redirect) {
    URI redirectUri = RequestUtils.sneakyUri(redirect);
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    AuthorizationRequest authorizationRequest =
        new AuthorizationRequest(email, redirectUri, serverBaseUri);

    return accountsService
        .chooseAccount(authorizationRequest)
        .thenApply(ChooseAccountResponse::build);
  }
}
