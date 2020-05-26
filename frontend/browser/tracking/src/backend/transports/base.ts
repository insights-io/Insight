/* eslint-disable no-restricted-globals */
/* eslint-disable no-nested-ternary */
import { EventData } from 'backend/types';

export class Status {
  public static Unknown = 'unknown';
  public static Success = 'success';
  public static Invalid = 'invalid';
  public static RateLimit = 'rate_limit';
  public static Failed = 'failed';

  public static fromHttpCode = (code: number): Status => {
    if (code >= 200 && code < 300) {
      return Status.Success;
    }

    if (code === 429) {
      return Status.RateLimit;
    }

    if (code >= 400 && code < 500) {
      return Status.Invalid;
    }

    if (code >= 500) {
      return Status.Failed;
    }

    return Status.Unknown;
  };
}

export type TransportResponse = {
  status: Status;
};

export interface BaseTransport {
  sendEvents: (url: string, data: EventData) => Promise<TransportResponse>;

  send: (url: string, data: string) => Promise<TransportResponse>;
}

export type PostResponse<T> = {
  status: number;
  json: Promise<T>;
};

export interface RequestResponseTransport extends BaseTransport {
  post: <T>(url: string, data: string) => Promise<PostResponse<T>>;
}

// eslint-disable-next-line @typescript-eslint/ban-types
export type GlobalObject = Window | NodeJS.Global | {};

export function getGlobalObject(): GlobalObject {
  return typeof window !== 'undefined'
    ? window
    : typeof global !== 'undefined'
    ? global
    : typeof self !== 'undefined'
    ? self
    : {};
}
