import React from 'react';
import { configureStory } from '@rebrowse/storybook';
import { SessionApi } from 'api/session';
import { REBROWSE_SESSIONS, CONSOLE_EVENTS, ERROR_EVENTS } from 'test/data';
import type { Meta } from '@storybook/react';

import { SessionDetails } from './SessionDetails';

export default {
  title: 'sessions/components/SessionDetails',
  component: SessionDetails,
} as Meta;

export const Base = () => {
  return <SessionDetails session={REBROWSE_SESSIONS[0]} />;
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
