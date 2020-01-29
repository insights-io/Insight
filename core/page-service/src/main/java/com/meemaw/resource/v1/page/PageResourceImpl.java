package com.meemaw.resource.v1.page;

import com.meemaw.model.page.Page;
import com.meemaw.model.page.PageDTO;
import com.meemaw.rest.response.DataResponse;
import com.meemaw.service.page.PageService;
import io.vertx.core.http.HttpServerRequest;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

public class PageResourceImpl implements PageResource {

    @Inject
    PageService pageService;

    @Context
    HttpServerRequest request;


    @Override
    public CompletionStage<Response> page(PageDTO payload) {
        Page page = Page.from(payload);
        return pageService.process(page).thenApply(DataResponse::ok);
    }
}
