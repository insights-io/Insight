import { BrowserEventArguments } from 'event';
import { EventType } from '@insight/types';

/* eslint-disable camelcase */
export interface InsightWindow {
  _i_org: string;
  _i_host: string;
  _i_log_level: string;
}

export type Enqueue = (
  eventType: EventType,
  args: BrowserEventArguments,
  eventName: string,
  event?: Event
) => void;
