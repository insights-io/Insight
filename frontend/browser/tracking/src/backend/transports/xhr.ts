import { EventData } from 'backend/types';

import { Status, RequestResponseTransport, PostResponse } from './base';

/** `XHR` based transport */
export class XHRTransport implements RequestResponseTransport {
  public post = <T>(url: string, body: string) => {
    return new Promise<PostResponse<T>>((resolve, reject) => {
      const xhr = new XMLHttpRequest();

      xhr.onreadystatechange = () => {
        if (xhr.readyState !== 4) {
          return;
        }

        if (xhr.status === 200) {
          resolve({ status: xhr.status, json: xhr.response });
        }

        reject(xhr);
      };

      xhr.open('POST', url);
      xhr.send(body);
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
}
