package com.rebrowse.beacon.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rebrowse.events.model.Recorded;
import com.rebrowse.events.model.incoming.AbstractBrowserEvent;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class BeaconDTO extends Recorded {

  @JsonProperty("s")
  @NotNull(message = "s may not be null")
  @Min(message = "s must be greater than 0", value = 1)
  int sequence;

  @JsonProperty("e")
  @NotEmpty(message = "e may not be empty")
  List<AbstractBrowserEvent<?>> events;
}
