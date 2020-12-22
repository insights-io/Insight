import {
  BrowserLogEventDTO,
  BrowserErrorEventDTO,
  BrowserXhrEventDTO,
} from '@rebrowse/types';

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

const INFO_NESTED_LOG: BrowserLogEventDTO = {
  e: 9,
  level: 'info',
  arguments: ['Debug message', [{ message: 'Nested' }]],
  t: 1001,
};

export const CONSOLE_EVENTS = [
  STORYBOOK_WARN,
  FAST_REFRESH_LOG,
  ERROR_LOG,
  DEBUG_LOG,
  INFO_NESTED_LOG,
];

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

const TYPE_ERROR: BrowserErrorEventDTO = {
  t: 1000.180000007618,
  e: 10,
  message: 'x is not a function',
  name: 'TypeError',
  stack: 'TypeError: x is not a function',
};

export const ERROR_EVENTS = [ERROR, SYNTAX_ERROR, TYPE_ERROR];

const CREATE_PAGE_EVENT: BrowserXhrEventDTO = {
  method: 'POST',
  url: 'http://localhost:8082/v1/sessions',
  status: 200,
  type: 'cors',
  t: 287,
  e: 11,
  initiatorType: 'xmlhttprequest',
  nextHopProtocol: 'h2',
};

const GET_SESSION_EVENT: BrowserXhrEventDTO = {
  method: 'GET',
  url: 'http://localhost:8082/v1/sessions/d1ae54f7-e285-4bbf-bbeb-3bdc0bc7b0ba',
  status: 200,
  type: 'cors',
  t: 338,
  e: 11,
  initiatorType: 'fetch',
  nextHopProtocol: 'http/1.1',
};

const BEACON_BEAT_EVENT: BrowserXhrEventDTO = {
  method: 'POST',
  url:
    'http://localhost:8081/v1/beacon/beat?organizationId=000000&sessionId=d1ae54f7-e285-4bbf-bbeb-3bdc0bc7b0ba&deviceId=1978361a-dfae-4801-8d84-89dd6af21740&pageVisitId=032ba89d-0d8b-4f4e-b60f-516f8291e739',
  status: 204,
  type: 'cors',
  t: 20739,
  e: 11,
  initiatorType: 'fetch',
  nextHopProtocol: 'http/1.1',
};

const NEXT_STACK_FRAME_EVENT: BrowserXhrEventDTO = {
  method: 'GET',
  url:
    '/__nextjs_original-stack-frame?isServerSide=false&file=file%3A%2F%2F%2FUsers%2Fmatejsnuderl%2FWorkspace%2Fpersonal%2Finsight%2Ffrontend%2Fapp%2F.next%2Fstatic%2Fdevelopment%2Fdll%2Fdll_bf9f5a6409814f8e5869.js&methodName=reconcileChildren&arguments=&lineNumber=17021&column=28',
  status: 204,
  type: null,
  t: 2924,
  e: 11,
  initiatorType: 'xmlhttprequest',
};

export const FETCH_EVENTS = [
  CREATE_PAGE_EVENT,
  GET_SESSION_EVENT,
  BEACON_BEAT_EVENT,
  NEXT_STACK_FRAME_EVENT,
];

export const REBROWSE_EVENTS = [
  ...CONSOLE_EVENTS,
  ...ERROR_EVENTS,
  ...FETCH_EVENTS,
];
