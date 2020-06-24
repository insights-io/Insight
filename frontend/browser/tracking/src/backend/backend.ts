/* eslint-disable no-underscore-dangle */
import { logger } from 'logger';
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
    beaconApiBaseURL: string,
    sessionApiBaseURL: string,
    organizationId: string
  ) {
    this.beaconURL = `${beaconApiBaseURL}/v1/beacon/beat?organizationId=${organizationId}`;
    this.pageURL = `${sessionApiBaseURL}/v1/sessions`;
    this.beaconSeq = 0;

    const globalObject = getGlobalObject();
    if (FetchTranport.isSupported(globalObject)) {
      this.requestResponseTransport = new FetchTranport();
      if (process.env.NODE_ENV !== 'production') {
        logger.debug('FetchTransport enabled');
      }
    } else {
      this.requestResponseTransport = new XHRTransport();
      if (process.env.NODE_ENV !== 'production') {
        logger.debug('XHRTransport enabled');
      }
    }

    if (BeaconTransport.isSupported(globalObject)) {
      this.maybeBeaconTransport = new BeaconTransport();
      if (process.env.NODE_ENV !== 'production') {
        logger.debug('BeaconTransport enabled');
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

  // TODO: better error handling
  private _sendEvents = (transport: BaseTransport, e: BrowserEvent[]) => {
    this.beaconSeq += 1;
    return transport
      .sendEvents(this.beaconURL, { e, s: this.beaconSeq })
      .then((response) => {
        if (response.status > 400 && response.status < 600) {
          throw new Error(`Failed to create page status: ${response.status}`);
        }
        return response;
      });
  };

  // TODO: better error handling
  public page = (pageDTO: CreatePageDTO) => {
    return this.requestResponseTransport
      .post<CreatePageResponse>(this.pageURL, JSON.stringify(pageDTO))
      .then((response) => {
        if (response.status > 400 && response.status < 600) {
          throw new Error(`Failed to create page status: ${response.status}`);
        }
        return response.json;
      });
  };
}

export default Backend;
