/* eslint-disable no-underscore-dangle */
/* eslint-disable no-console */
import { BrowserEvent } from 'event';
import { Connected } from 'identity/types';
import {
  CreatePageResponse,
  PageIdentity,
  CreatePageDTO,
} from '@insight/types';

import { BeaconTransport } from './transports/beacon';
import {
  BaseTransport,
  getGlobalObject,
  RequestResponseTransport,
} from './transports/base';
import { FetchTranport } from './transports/fetch';
import { XHRTransport } from './transports/xhr';

class Backend implements Connected {
  private readonly requestResponseTransport: RequestResponseTransport;
  private readonly maybeBeaconTransport: BaseTransport;
  private readonly pageURL: string;

  private beaconURL: string;
  private beaconSeq: number;

  constructor(
    recordingApiBaseURL: string,
    sessionApiBaseURL: string,
    organizationId: string
  ) {
    this.beaconURL = `${recordingApiBaseURL}/v1/beacon/beat?organizationId=${organizationId}`;
    this.pageURL = `${sessionApiBaseURL}/v1/sessions`;
    this.beaconSeq = 0;

    const globalObject = getGlobalObject();
    if (FetchTranport.isSupported(globalObject)) {
      this.requestResponseTransport = new FetchTranport();
      if (process.env.NODE_ENV !== 'production') {
        console.debug('FetchTransport enabled');
      }
    } else {
      this.requestResponseTransport = new XHRTransport();
      if (process.env.NODE_ENV !== 'production') {
        console.debug('XHRTransport enabled');
      }
    }

    if (BeaconTransport.isSupported(globalObject)) {
      this.maybeBeaconTransport = new BeaconTransport();
      if (process.env.NODE_ENV !== 'production') {
        console.debug('BeaconTransport enabled');
      }
    } else {
      this.maybeBeaconTransport = this.requestResponseTransport;
    }
  }

  public sendEvents = (e: BrowserEvent[]) => {
    return this._sendEvents(this.requestResponseTransport, e);
  };

  public sendBeacon = (e: BrowserEvent[]) => {
    return this._sendEvents(this.maybeBeaconTransport, e);
  };

  public connect = (identity: PageIdentity) => {
    const { sessionId, deviceId, pageId } = identity;
    this.beaconURL = `${this.beaconURL}&sessionId=${sessionId}&deviceId=${deviceId}&pageId=${pageId}`;
  };

  private _sendEvents = (transport: BaseTransport, e: BrowserEvent[]) => {
    this.beaconSeq += 1;
    return transport.sendEvents(this.beaconURL, { e, s: this.beaconSeq });
  };

  public page = (pageDTO: CreatePageDTO) => {
    return this.requestResponseTransport
      .post<CreatePageResponse>(this.pageURL, JSON.stringify(pageDTO))
      .then((response) => response.json);
  };
}

export default Backend;
