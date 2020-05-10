package com.meemaw.events.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

@ToString
public abstract class Recorded {

  @Getter
  @JsonProperty("t")
  @NotNull(message = "t may not be null")
  @Min(message = "t must be non negative", value = 0)
  protected int timestamp;

}
