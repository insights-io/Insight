/* eslint-disable no-underscore-dangle */
import { logger } from 'logger';
import type { BrowserEvent } from 'event';
import type { Connected } from 'identity/types';
import type {
  CreatePageResponse,
  PageVisitSessionLink,
  CreatePageVisitDTO,
} from '@rebrowse/types';
import Context from 'context';

import { BeaconTransport } from './transports/beacon';
import { BaseTransport, RequestResponseTransport } from './transports/base';
import { FetchTranport } from './transports/fetch';
import { XHRTransport } from './transports/xhr';

class Backend implements Connected {
  private readonly _context: Context;
  private readonly _requestResponseTransport: RequestResponseTransport;
  private readonly _maybeBeaconTransport: BaseTransport;
  private readonly _pageVisitURL: string;

  private _beaconURL: string;

  constructor(
    beaconApiBaseURL: string,
    sessionApiBaseURL: string,
    organizationId: string,
    context: Context
  ) {
    this._context = context;
    this._beaconURL = `${beaconApiBaseURL}/v1/beacon/beat?organizationId=${organizationId}`;
    this._pageVisitURL = `${sessionApiBaseURL}/v1/pages`;

    const globalObject = Context.getGlobalObject();
    if (FetchTranport.isSupported(globalObject)) {
      this._requestResponseTransport = new FetchTranport();
      if (process.env.NODE_ENV !== 'production') {
        logger.debug('FetchTransport enabled');
      }
    } else {
      this._requestResponseTransport = new XHRTransport();
      if (process.env.NODE_ENV !== 'production') {
        logger.debug('XHRTransport enabled');
      }
    }

    if (BeaconTransport.isSupported(globalObject)) {
      this._maybeBeaconTransport = new BeaconTransport();
      if (process.env.NODE_ENV !== 'production') {
        logger.debug('BeaconTransport enabled');
      }
    } else {
      this._maybeBeaconTransport = this._requestResponseTransport;
    }
  }

  public sendEvents = (e: BrowserEvent[]) => {
    return this._sendEvents(this._requestResponseTransport, e);
  };

  public sendBeacon = (e: BrowserEvent[]) => {
    return this._sendEvents(this._maybeBeaconTransport, e);
  };

  public connect = (identity: PageVisitSessionLink) => {
    const { sessionId, deviceId, pageVisitId } = identity;
    this._beaconURL = `${this._beaconURL}&sessionId=${sessionId}&deviceId=${deviceId}&pageVisitId=${pageVisitId}`;
  };

  // TODO: better error handling
  private _sendEvents = (transport: BaseTransport, e: BrowserEvent[]) => {
    const s = this._context.incrementAndGetSeq();
    return transport.sendEvents(this._beaconURL, { e, s }).then((response) => {
      if (response.status > 400 && response.status < 600) {
        throw new Error(`Failed to create page status: ${response.status}`);
      }
      return response;
    });
  };

  // TODO: better error handling
  public page = (pageDTO: CreatePageVisitDTO) => {
    return this._requestResponseTransport
      .post<CreatePageResponse>(this._pageVisitURL, JSON.stringify(pageDTO))
      .then((response) => {
        if (response.status > 400 && response.status < 600) {
          throw new Error(`Failed to create page status: ${response.status}`);
        }
        return response.json;
      });
  };
}

export default Backend;
