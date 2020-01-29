package com.meemaw.model.beacon;

import java.util.List;
import java.util.Objects;

public class Beacon {

    private final int timestamp;
    private final int sequence;
    private final List<BeaconEventDTO> events;

    private Beacon(BeaconDTO dto) {
        this.timestamp = dto.getTimestamp();
        this.sequence = dto.getSequence();
        this.events = dto.getEvents();
    }

    public static Beacon from(BeaconDTO dto) {
        return new Beacon(Objects.requireNonNull(dto));
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getSequence() {
        return sequence;
    }

    public List<BeaconEventDTO> getEvents() {
        return events;
    }
}
