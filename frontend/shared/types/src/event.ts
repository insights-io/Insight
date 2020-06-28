type SequenceID = number;
type Timestamp = number;

export const enum EventType {
  NAVIGATE = '0',
  UNLOAD = '1',
  RESIZE = '2',
  PERFORMANCE = '3',
  CLICK = '4',
  MOUSEMOVE = '5',
  MOUSEDOWN = '6',
  MOUSEUP = '7',
  LOAD = '8',
  LOG = '9',
}

export type AbstractBeaconEvent = {
  t: Timestamp;
  e: EventType;
  a: (string | number)[];
};

export type Beacon = {
  e: AbstractBeaconEvent[];
  s: SequenceID;
};

type AbstractBrowserEventDTO = {
  t: Timestamp;
  e: EventType;
};

export type LogLevel = 'info' | 'debug' | 'log' | 'error' | 'warn';

export interface BrowserLogEventDTO extends AbstractBrowserEventDTO {
  e: EventType.LOG;
  level: LogLevel;
  arguments: string[];
}

export type BrowserEventDTO = BrowserLogEventDTO;
