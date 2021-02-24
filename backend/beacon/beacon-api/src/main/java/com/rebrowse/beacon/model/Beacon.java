package com.rebrowse.beacon.model;

import com.rebrowse.beacon.model.dto.BeaconDTO;
import com.rebrowse.events.model.incoming.AbstractBrowserEvent;
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
  List<AbstractBrowserEvent<?>> events;

  Beacon(BeaconDTO dto) {
    this(dto.getTimestamp(), dto.getSequence(), dto.getEvents());
  }

  public static Beacon from(BeaconDTO dto) {
    return new Beacon(Objects.requireNonNull(dto));
  }
}
