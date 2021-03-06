import { EventData } from 'event';
import { GlobalObject } from 'context/Context';

import { Status, RequestResponseTransport, ContentType } from './base';

/** `fetch` based transport */
export class FetchTranport implements RequestResponseTransport {
  public post = (url: string, body: string) => {
    return fetch(url, {
      body,
      method: 'POST',
      headers: { 'Content-Type': ContentType.JSON },
    }).then((response) => {
      if (response.headers.get('Content-type') === ContentType.JSON) {
        return response
          .json()
          .then((json) => ({ status: response.status, json }));
      }
      return { status: response.status, json: undefined };
    });
  };

  public send = (url: string, body: string) => {
    return this.post(url, body).then(({ status }) => {
      return { status: Status.fromHttpCode(status) };
    });
  };

  public sendEvents = (url: string, data: EventData) => {
    return this.send(url, JSON.stringify(data));
  };

  public static isSupported = (
    global: GlobalObject
  ): global is Window | NodeJS.Global => {
    if (!('fetch' in global)) {
      return false;
    }

    try {
      // eslint-disable-next-line no-new
      new Headers();
      // eslint-disable-next-line no-new
      new Request('');
      // eslint-disable-next-line no-new
      new Response();
      return true;
    } catch (e) {
      return false;
    }
  };
}
