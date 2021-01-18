/* eslint-disable no-underscore-dangle */
import { logger } from 'logger';
import type { BrowserEvent } from 'event';
import type { Connected } from 'identity/types';
import type {
  CreatePageResponse,
  PageVisitSessionLink,
  PageVisitCreateParams,
} from '@rebrowse/types';
import Context from 'context';

import { BeaconTransport } from './transports/beacon';
import { BaseTransport, RequestResponseTransport } from './transports/base';
import { FetchTranport } from './transports/fetch';
import { XHRTransport } from './transports/xhr';

export const beaconBeatBaseUrl = (beaconApiBaseUrl: string) => {
  return `${beaconApiBaseUrl}/v1/recording/beat`;
};

export const pageVisitBaseUrl = (sessionApiBaseUrl: string) => {
  return `${sessionApiBaseUrl}/v1/pages`;
};

class Backend implements Connected {
  private readonly _context: Context;
  private readonly requestResponseTransport: RequestResponseTransport;
  private readonly maybeBeaconTransport: BaseTransport;
  private readonly pageVisitApiUrl: string;

  private beaconApiUrl: string;

  constructor(
    beaconApiBaseUrl: string,
    sessionApiBaseUrl: string,
    organizationId: string,
    context: Context
  ) {
    this._context = context;
    this.pageVisitApiUrl = pageVisitBaseUrl(sessionApiBaseUrl);
    this.beaconApiUrl = `${beaconBeatBaseUrl(
      beaconApiBaseUrl
    )}?organizationId=${organizationId}`;

    const globalObject = Context.getGlobalObject();
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

  public connect = (identity: PageVisitSessionLink) => {
    const { sessionId, deviceId, pageVisitId } = identity;
    this.beaconApiUrl = `${this.beaconApiUrl}&sessionId=${sessionId}&deviceId=${deviceId}&pageVisitId=${pageVisitId}`;
  };

  // TODO: better error handling
  private _sendEvents = (transport: BaseTransport, e: BrowserEvent[]) => {
    const s = this._context.incrementAndGetSeq();
    return transport
      .sendEvents(this.beaconApiUrl, { e, s })
      .then((response) => {
        if (response.status > 400 && response.status < 600) {
          throw new Error(`Failed to create page status: ${response.status}`);
        }
        return response;
      });
  };

  // TODO: better error handling
  public page = (createParams: PageVisitCreateParams) => {
    return this.requestResponseTransport
      .post<CreatePageResponse>(
        this.pageVisitApiUrl,
        JSON.stringify(createParams)
      )
      .then((response) => {
        if (response.status > 400 && response.status < 600) {
          throw new Error(`Failed to create page status: ${response.status}`);
        }
        return response.json;
      });
  };
}

export default Backend;
