package com.meemaw.search.events.resource;

import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(EventsResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface EventsResource {

  String PATH = "v1/events";

  @GET
  CompletionStage<String> search();

}
