package com.meemaw.events.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Data
public class Recorded {

  public static final String TIMESTAMP = "t";

  @Getter
  @JsonProperty(TIMESTAMP)
  @NotNull(message = "t may not be null")
  @Min(message = "t must be non negative", value = 0)
  protected int timestamp;
}
