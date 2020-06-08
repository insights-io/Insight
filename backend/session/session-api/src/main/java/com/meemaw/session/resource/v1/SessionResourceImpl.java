package com.meemaw.session.resource.v1;

import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.service.PageService;
import com.meemaw.session.service.SessionSearchService;
import com.meemaw.session.service.SessionSocketService;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionResourceImpl implements SessionResource {

  @Inject PageService pageService;
  @Inject SessionSocketService sessionSocketService;
  @Inject SessionSearchService sessionSearchService;

  @Override
  public CompletionStage<Response> createPage(CreatePageDTO body) {
    return pageService
        .createPage(body)
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
  public CompletionStage<Response> getPage(UUID sessionId, UUID pageId, String organizationId) {
    return pageService
        .getPage(pageId, sessionId, organizationId)
        .subscribeAsCompletionStage()
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> search() {
    return sessionSearchService.search().thenApply(DataResponse::ok);
  }
}
