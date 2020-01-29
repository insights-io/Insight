package com.meemaw.resource.v1.beacon;

import com.meemaw.model.beacon.Beacon;
import com.meemaw.model.beacon.BeaconDTO;
import com.meemaw.rest.response.DataResponse;
import com.meemaw.service.beacon.BeaconService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

public class BeaconResourceImpl implements BeaconResource {

    @Inject
    BeaconService beaconService;

    @Override
    public CompletionStage<Response> beacon(BeaconDTO payload) {
        Beacon beacon = Beacon.from(payload);
        return beaconService.process(beacon).thenApply(DataResponse::ok);
    }
}
