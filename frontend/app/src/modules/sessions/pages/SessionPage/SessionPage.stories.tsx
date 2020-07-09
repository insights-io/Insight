import React from 'react';
import {
  INSIGHT_SESSION,
  INSIGHT_SESSION_DTO,
  STORYBOK_CONSOLE_WARN_EVENT,
  FAST_REFRESH_CONSOLE_LOG_EVENT,
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
        .stub(SessionApi.events, 'get')
        .resolves([
          FAST_REFRESH_CONSOLE_LOG_EVENT,
          STORYBOK_CONSOLE_WARN_EVENT,
        ]),
    };
  },
});
