import { Enqueue } from 'types';
import { EventType } from 'event';

/* eslint-disable no-console */
export const LOG_LEVELS = {
  debug: 10,
  log: 20,
  info: 20,
  warn: 30,
  error: 40,
} as const;

export type LogLevel = keyof typeof LOG_LEVELS;

export const proxyConsoleLog = (enqueue: Enqueue) => {
  Object.keys(LOG_LEVELS).forEach((logLevel) => {
    const typedLogLevel = logLevel as LogLevel;
    const originalConsoleLog = console[typedLogLevel];

    console[typedLogLevel] = (...args: never[]) => {
      originalConsoleLog(...args);
      enqueue(EventType.LOG, [typedLogLevel, ...args], `[console.${logLevel}]`);
    };
  });
};
