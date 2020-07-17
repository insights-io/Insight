/* eslint-disable lodash/prefer-lodash-typecheck */
/* eslint-disable no-console */
import { Enqueue } from 'types';
import { EventType, LogLevel } from '@insight/types';

import { getGlobalObject, GlobalObject } from '../backend/transports/base';
import { FetchTranport } from '../backend/transports/fetch';

import { monkeyPatch } from './patch';

export function insrumentXMLHttpRequest(
  _enqueue: Enqueue
): XMLHttpRequest['send'] | undefined {
  const xhrproto = XMLHttpRequest.prototype;

  return monkeyPatch(xhrproto, 'send', (original) => {
    return (...args) => {
      return original.apply(xhrproto, args);
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
            EventType.FETCH,
            [method, url, response.status, response.type],
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
      enqueue(
        EventType.LOG,
        [typedLogLevel, JSON.stringify(args)],
        `[console.${logLevel}]`
      );
    };
  });

  return console;
};

export function instrumentGlobals(enqueue: Enqueue) {
  const globalObject = getGlobalObject();
  instrumentFetch(globalObject, enqueue);
  instrumentConsole(enqueue);
  insrumentXMLHttpRequest(enqueue);
}
