import { EventData } from 'event';
import { GlobalObject } from 'context/Context';

import { BaseTransport, Status } from './base';

/** `sendBeacon` based transport */
export class BeaconTransport implements BaseTransport {
  public send = (url: string, data: string) => {
    const result = navigator.sendBeacon(url, data);
    return Promise.resolve({ status: result ? Status.Success : Status.Failed });
  };

  public sendEvents = (url: string, eventsData: EventData) => {
    return this.send(url, JSON.stringify(eventsData));
  };

  public static isSupported = (global: GlobalObject) => {
    return 'navigator' in global && 'sendBeacon' in global.navigator;
  };
}
