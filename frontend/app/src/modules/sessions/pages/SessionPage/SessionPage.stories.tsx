import React from 'react';
import {
  INSIGHT_SESSION,
  INSIGHT_SESSION_DTO,
  CONSOLE_EVENTS,
  ERROR_EVENTS,
} from 'test/data';
import { configureStory, fullHeightDecorator } from '@insight/storybook';
import { SessionApi } from 'api';

import SessionPage from './SessionPage';

export default {
  title: 'sessions|pages/SessionPage',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <SessionPage sessionId={INSIGHT_SESSION.id} session={INSIGHT_SESSION} />
  );
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      getSessions: sandbox
        .stub(SessionApi, 'getSessions')
        .resolves([INSIGHT_SESSION_DTO]),
      getEvents: sandbox
        .stub(SessionApi.events, 'search')
        .resolves([
          CONSOLE_EVENTS.FAST_REFRESH_LOG,
          CONSOLE_EVENTS.STORYBOOK_WARN,
          CONSOLE_EVENTS.ERROR_LOG,
          CONSOLE_EVENTS.DEBUG_LOG,
          ERROR_EVENTS.ERROR,
          ERROR_EVENTS.SYNTAX_ERROR,
        ]),
    };
  },
});
