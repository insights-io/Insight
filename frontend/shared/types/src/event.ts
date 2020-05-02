type SequenceID = number;
type Timestamp = number;
type EventType = number;

export type AbstractBeaconEvent = {
  t: Timestamp;
  e: EventType;
  a: (string | number)[];
};

export type Beacon = {
  e: AbstractBeaconEvent[];
  s: SequenceID;
};
