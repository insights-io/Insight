package com.meemaw.resource.v1.beacon;

import com.meemaw.model.beacon.BeaconDTO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

@Path(BeaconResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BeaconResource {

    String PATH = "v1/beacon";

    @POST
    CompletionStage<Response> beacon(@NotNull(message = "Payload may not be blank")
                                     @Valid BeaconDTO beacon);
}
