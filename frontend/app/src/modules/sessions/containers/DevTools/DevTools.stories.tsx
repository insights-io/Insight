import React from 'react';
import { configureStory } from '@insight/storybook';
import { SessionApi } from 'api';
import { CONSOLE_EVENTS, ERROR_EVENTS } from 'test/data';

import DevTools from './DevTools';

export default {
  title: 'sessions|containers/DevTools',
};

export const Base = () => {
  return <DevTools sessionId="10" />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'get')
      .resolves([
        CONSOLE_EVENTS.FAST_REFRESH_LOG,
        CONSOLE_EVENTS.STORYBOOK_WARN,
        CONSOLE_EVENTS.ERROR_LOG,
        CONSOLE_EVENTS.DEBUG_LOG,
        ERROR_EVENTS.ERROR,
        ERROR_EVENTS.SYNTAX_ERROR,
      ]);
  },
});
