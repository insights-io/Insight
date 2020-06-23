/* eslint-disable no-console */
const LOG_LEVELS = {
  debug: 10,
  info: 20,
  warn: 30,
  error: 40,
} as const;

type LogLevel = keyof typeof LOG_LEVELS;

const getLogLevel = (): number => {
  // eslint-disable-next-line no-underscore-dangle
  const logLevel = (window._i_log_level || 'info').toLowerCase() as LogLevel;
  return LOG_LEVELS[logLevel] || 0;
};

export const logger = Object.keys(LOG_LEVELS).reduce((acc, logLevel) => {
  const typedLogLevel = logLevel as LogLevel;
  return {
    ...acc,
    [typedLogLevel]: (...data: never[]) => {
      if (getLogLevel() >= LOG_LEVELS[typedLogLevel]) {
        console[typedLogLevel](data);
      }
    },
  };
}, {} as Record<LogLevel, typeof console['log']>);
