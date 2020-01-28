package com.meemaw.model.beacon;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public abstract class Recorded {

    @JsonProperty("t")
    @NotNull(message = "t may not be null")
    @Min(message = "t must be non negative", value = 0)
    private int timestamp;

    public int getTimestamp() {
        return timestamp;
    }
}
