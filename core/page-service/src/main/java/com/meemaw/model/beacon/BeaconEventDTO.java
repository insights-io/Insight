package com.meemaw.model.beacon;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class BeaconEventDTO extends Recorded {

    @JsonProperty("k")
    @NotNull(message = "k may not be null")
    private BeaconEventType beaconEventType;

    @JsonProperty("a")
    @NotNull(message = "a may not be null")
    private List<Object> args;

}
