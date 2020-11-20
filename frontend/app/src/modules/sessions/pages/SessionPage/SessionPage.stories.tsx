import React from 'react';
import {
  REBROWSE_SESSION,
  REBROWSE_SESSION_DTO,
  CONSOLE_EVENTS,
  ERROR_EVENTS,
  REBROWSE_ADMIN_DTO,
  REBROWSE_ORGANIZATION_DTO,
} from 'test/data';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { SessionApi } from 'api';
import { Meta } from '@storybook/react';

import { SessionPage } from './SessionPage';

export default {
  title: 'sessions/pages/SessionPage',
  component: SessionPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <SessionPage
      sessionId={REBROWSE_SESSION.id}
      session={REBROWSE_SESSION_DTO}
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      getSessions: sandbox
        .stub(SessionApi, 'getSessions')
        .resolves([REBROWSE_SESSION_DTO]),
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
