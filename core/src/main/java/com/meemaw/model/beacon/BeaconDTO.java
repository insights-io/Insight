package com.meemaw.model.beacon;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class BeaconDTO extends Recorded {

    @JsonProperty("s")
    @NotNull(message = "s may not be null")
    @Min(message = "s must be greater than 0", value = 1)
    private int sequence;

    @JsonProperty("e")
    @NotNull(message = "e may not be null")
    private List<BeaconEventDTO> events;

    public int getSequence() {
        return sequence;
    }

    public List<BeaconEventDTO> getEvents() {
        return events;
    }
}
