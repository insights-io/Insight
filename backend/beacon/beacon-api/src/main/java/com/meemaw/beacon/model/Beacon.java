package com.meemaw.beacon.model;

import com.meemaw.beacon.model.dto.BeaconDTO;
import com.meemaw.events.model.internal.AbstractBrowserEvent;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Beacon {

  int timestamp;
  int sequence;
  List<AbstractBrowserEvent> events;

  Beacon(BeaconDTO dto) {
    this(dto.getTimestamp(), dto.getSequence(), dto.getEvents());
  }

  public static Beacon from(BeaconDTO dto) {
    return new Beacon(Objects.requireNonNull(dto));
  }
}
