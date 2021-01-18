import type { LogLevel } from '@rebrowse/types';

import { LOG_LEVELS } from '../instrument';

const getLogLevel = (): number => {
  // eslint-disable-next-line no-underscore-dangle
  const logLevel = (window._i_log_level || 'log').toLowerCase() as LogLevel;
  return LOG_LEVELS[logLevel] || 0;
};

export const logger = Object.keys(LOG_LEVELS).reduce((acc, logLevel) => {
  const typedLogLevel = logLevel as LogLevel;
  const currentLogLevelValue = LOG_LEVELS[typedLogLevel];

  return {
    ...acc,
    [typedLogLevel]: (...data: never[]) => {
      if (currentLogLevelValue >= getLogLevel()) {
        // eslint-disable-next-line no-console
        console[typedLogLevel](...data);
      }
    },
  };
}, {} as Record<LogLevel, typeof console['log']>);
