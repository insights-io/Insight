package com.meemaw.resource.v1.page;

import com.meemaw.model.page.PageDTO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

@Path(PageResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface PageResource {

    String PATH = "v1/page";

    @POST
    CompletionStage<Response> page(@NotNull(message = "Payload may not be blank") @Valid PageDTO payload);
}
