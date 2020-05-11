package com.meemaw.session.resource.v1;

import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.service.PageService;
import com.meemaw.session.service.SessionSocketService;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class SessionResourceImpl implements SessionResource {

  @Inject PageService pageService;

  @Inject SessionSocketService sessionSocketService;

  @Override
  public CompletionStage<Response> page(CreatePageDTO page) {
    return pageService
        .process(page)
        .subscribeAsCompletionStage()
        .thenApply(
            pageIdentity -> {
              sessionSocketService.pageStart(pageIdentity.getPageId());
              return DataResponse.ok(pageIdentity);
            });
  }

  @Override
  public CompletionStage<Response> count() {
    return pageService.activePageCount().subscribeAsCompletionStage().thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> get(UUID sessionID, UUID pageID, String orgID) {
    return pageService
        .getPage(pageID, sessionID, orgID)
        .subscribeAsCompletionStage()
        .thenApply(
            pageDTO -> DataResponse.ok(pageDTO.orElseThrow(() -> Boom.notFound().exception())));
  }
}
