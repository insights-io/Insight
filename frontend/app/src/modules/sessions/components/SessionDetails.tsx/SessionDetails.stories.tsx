import React from 'react';
import { configureStory } from '@insight/storybook';
import SessionApi from 'api/session';
import { INSIGHT_SESSION, CONSOLE_EVENTS, ERROR_EVENTS } from 'test/data';

import SessionDetails from './SessionDetails';

export default {
  title: 'sessions/components/SessionDetails',
};

export const Base = () => {
  return <SessionDetails session={INSIGHT_SESSION} />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'search')
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
