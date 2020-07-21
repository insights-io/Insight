type SequenceID = number;
type Timestamp = number;

export const enum EventType {
  NAVIGATE = 0,
  UNLOAD = 1,
  RESIZE = 2,
  PERFORMANCE = 3,
  CLICK = 4,
  MOUSEMOVE = 5,
  MOUSEDOWN = 6,
  MOUSEUP = 7,
  LOAD = 8,
  LOG = 9,
  ERROR = 10,
  FETCH = 11,
  RESOURCE_PERFORMANCE = 12,
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
  arguments: unknown[];
}

export interface BrowserErrorEventDTO extends AbstractBrowserEventDTO {
  e: EventType.ERROR;
  message: string;
  name: string;
  stack: string;
}

export interface BrowserXhrEventDTO extends AbstractBrowserEventDTO {
  e: EventType.FETCH;
  method: string;
  url: string;
  status: number;
  type: string;
}

export interface BrowserResourcePerformanceEventDTO
  extends AbstractBrowserEventDTO {
  e: EventType.RESOURCE_PERFORMANCE;
  name: string;
  startTime: number;
  duration: number;
  initiatorType: string;
  nextHopProtocol: string;
}

export type BrowserEventDTO =
  | BrowserLogEventDTO
  | BrowserErrorEventDTO
  | BrowserXhrEventDTO
  | BrowserResourcePerformanceEventDTO;
