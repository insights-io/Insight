import { BrowserLogEventDTO, BrowserErrorEventDTO } from '@insight/types';

const STORYBOOK_WARN: BrowserLogEventDTO = {
  e: 9,
  level: 'warn',
  arguments: [
    `Vendors~main.6e71f8501d51c505cf1d.bundle.js:70044 The default hierarchy separators are changing in Storybook 6.0.
        '|' and '.' will no longer create a hierarchy, but codemods are available.
        Read more about it in the migration guide: https://github.com/storybookjs/storybook/blob/master/MIGRATION.md`,
  ],
  t: 1001,
};

const FAST_REFRESH_LOG: BrowserLogEventDTO = {
  e: 9,
  level: 'log',
  arguments: ['[Fast Refresh] done'],
  t: 999,
};

const ERROR_LOG: BrowserLogEventDTO = {
  e: 9,
  level: 'error',
  arguments: ['Something went wrong'],
  t: 1001,
};

const DEBUG_LOG: BrowserLogEventDTO = {
  e: 9,
  level: 'debug',
  arguments: ['Debug message'],
  t: 1001,
};

export const CONSOLE_EVENTS = {
  STORYBOOK_WARN,
  FAST_REFRESH_LOG,
  ERROR_LOG,
  DEBUG_LOG,
} as const;

const ERROR: BrowserErrorEventDTO = {
  e: 10,
  message: 'simulated error',
  name: 'Error',
  stack:
    'Error: simulated error\n    at <anonymous>:1:7\n    at eval (__playwright_evaluation_script__45:7:47)\n    at UtilityScript.callFunction (__playwright_evaluation_script__1:299:24)\n    at UtilityScript.<anonymous> (__playwright_evaluation_script__46:1:44)',
  t: 12047.294999996666,
};

const SYNTAX_ERROR: BrowserErrorEventDTO = {
  t: 12024.180000007618,
  e: 10,
  message: 'Unexpected identifier',
  name: 'SyntaxError',
  stack:
    'SyntaxError: Unexpected identifier\n    at <anonymous>:1:1\n    at eval (__playwright_evaluation_script__45:11:47)\n    at UtilityScript.callFunction (__playwright_evaluation_script__1:299:24)\n    at UtilityScript.<anonymous> (__playwright_evaluation_script__46:1:44)',
};

export const ERROR_EVENTS = {
  ERROR,
  SYNTAX_ERROR,
} as const;
