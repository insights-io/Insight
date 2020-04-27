import { EventData } from 'backend/types';

import { BaseTransport, Status, GlobalObject } from './base';

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
