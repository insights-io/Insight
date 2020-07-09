import React from 'react';
import { configureStory } from '@insight/storybook';
import SessionApi from 'api/session';
import {
  INSIGHT_SESSION,
  FAST_REFRESH_CONSOLE_LOG_EVENT,
  STORYBOK_CONSOLE_WARN_EVENT,
} from 'test/data';

import SessionDetails from './SessionDetails';

export default {
  title: 'SessionDetails',
};

export const Base = () => {
  return <SessionDetails session={INSIGHT_SESSION} />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'get')
      .resolves([FAST_REFRESH_CONSOLE_LOG_EVENT, STORYBOK_CONSOLE_WARN_EVENT]);
  },
});
