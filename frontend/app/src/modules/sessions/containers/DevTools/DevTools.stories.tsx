import React from 'react';
import { configureStory, fullHeightDecorator } from '@insight/storybook';
import { SessionApi } from 'api';
import { CONSOLE_EVENTS, ERROR_EVENTS, FETCH_EVENTS } from 'test/data';
import noop from 'lodash/noop';

import DevTools from './DevTools';

export default {
  title: 'sessions/containers/DevTools',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <DevTools sessionId="10" />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'search')
      .callsFake((_sessionId, options) => {
        if (
          JSON.stringify(options?.search?.['event.e']) ===
          JSON.stringify(['gte:9', 'lte:10'])
        ) {
          return Promise.resolve([
            CONSOLE_EVENTS.FAST_REFRESH_LOG,
            CONSOLE_EVENTS.STORYBOOK_WARN,
            CONSOLE_EVENTS.ERROR_LOG,
            CONSOLE_EVENTS.DEBUG_LOG,
            ERROR_EVENTS.ERROR,
            ERROR_EVENTS.SYNTAX_ERROR,
          ]);
        }
        return Promise.resolve([
          FETCH_EVENTS.BEACON_BEAT_EVENT,
          FETCH_EVENTS.CREATE_PAGE_EVENT,
          FETCH_EVENTS.GET_SESSION_EVENT,
          FETCH_EVENTS.NEXT_STACK_FRAME_EVENT,
        ]);
      });
  },
});

export const Loading = () => {
  return <DevTools sessionId="10" />;
};
Loading.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'search')
      .callsFake(() => new Promise(noop));
  },
});
