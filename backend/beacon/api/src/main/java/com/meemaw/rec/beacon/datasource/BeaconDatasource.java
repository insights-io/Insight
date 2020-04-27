package com.meemaw.rec.beacon.datasource;

import com.meemaw.rec.beacon.model.Beacon;
import io.smallrye.mutiny.Uni;

public interface BeaconDatasource {

  Uni<Void> store(Beacon beacon);
}
