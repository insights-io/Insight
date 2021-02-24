/* eslint-disable @typescript-eslint/no-this-alias */
/* eslint-disable camelcase */
/* eslint-disable func-names */
/* eslint-disable no-underscore-dangle */
/* eslint-disable no-console */
import { Enqueue } from 'types';
import { EventType, LogLevel } from '@rebrowse/types';
import Context from 'context';

import { FetchTranport } from '../backend/transports/fetch';
import { GlobalObject } from '../context/Context';

import { monkeyPatch } from './patch';

interface WrappedXMLHttpRequest extends XMLHttpRequest {
  __rebrowse_xhr__?: {
    url?: string;
    method?: string;
  };
}

export function insrumentXMLHttpRequest(
  enqueue: Enqueue
): XMLHttpRequest['send'] | undefined {
  const xhrproto = XMLHttpRequest.prototype;

  // @ts-expect-error its okay
  monkeyPatch(xhrproto, 'open', (original) => {
    return function (
      this: WrappedXMLHttpRequest,
      ...args: Parameters<XMLHttpRequest['open']>
    ) {
      const method = args[0].toUpperCase();
      const url = args[1];
      this.__rebrowse_xhr__ = { method, url };
      return original.apply(this, args);
    };
  });

  return monkeyPatch(xhrproto, 'send', (original) => {
    return function (this: WrappedXMLHttpRequest, ...args) {
      const xhr = this;

      // eslint-disable-next-line prefer-arrow-callback
      xhr.addEventListener('readystatechange', function () {
        if (xhr.readyState === 4) {
          enqueue(
            EventType.XHR,
            [
              xhr.__rebrowse_xhr__?.method as string,
              xhr.__rebrowse_xhr__?.url as string,
              xhr.status,
              null,
              'xmlhttprequest',
            ],
            '[xhr]'
          );
        }
      });

      return original.apply(this, args);
    };
  });
}

export function instrumentFetch(
  globalObject: GlobalObject,
  enqueue: Enqueue
): typeof global['fetch'] | undefined {
  if (!FetchTranport.isSupported(globalObject)) {
    return undefined;
  }

  type FetchArguments = Parameters<typeof global['fetch']>;

  const getFetchMethod = (args: FetchArguments): string => {
    if (args[0] instanceof Request && args[0].method) {
      return args[0].method.toUpperCase();
    }

    if (args[1] && args[1].method) {
      return args[1].method.toUpperCase();
    }

    return 'GET';
  };

  const getFetchUrl = (args: FetchArguments): string => {
    if (typeof args[0] === 'string') {
      return args[0];
    }

    if (args[0] instanceof Request) {
      return args[0].url;
    }

    return args[0];
  };

  return monkeyPatch(globalObject, 'fetch', (original) => {
    return (...args) => {
      const method = getFetchMethod(args);
      const url = getFetchUrl(args);

      return original
        .apply(globalObject, args)
        .then((response) => {
          enqueue(
            EventType.XHR,
            [method, url, response.status, response.type, 'fetch'],
            '[fetch]'
          );
          return response;
        })
        .catch((error) => {
          throw error;
        });
    };
  });
}

export const LOG_LEVELS: Record<LogLevel, number> = {
  debug: 10,
  log: 20,
  info: 20,
  warn: 30,
  error: 40,
};

export const instrumentConsole = (enqueue: Enqueue): Console => {
  Object.keys(LOG_LEVELS).forEach((logLevel) => {
    const typedLogLevel = logLevel as LogLevel;
    const originalConsoleLog = console[typedLogLevel];

    console[typedLogLevel] = (...args: never[]) => {
      originalConsoleLog(...args);
      enqueue(EventType.LOG, [typedLogLevel, ...args], `[console.${logLevel}]`);
    };
  });

  return console;
};

export function instrumentGlobals(enqueue: Enqueue) {
  const globalObject = Context.getGlobalObject();
  instrumentFetch(globalObject, enqueue);
  instrumentConsole(enqueue);
  insrumentXMLHttpRequest(enqueue);
}
