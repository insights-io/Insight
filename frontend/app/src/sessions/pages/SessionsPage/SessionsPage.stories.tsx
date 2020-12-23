import React from 'react';
import {
  REBROWSE_ADMIN_DTO,
  REBROWSE_SESSIONS,
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_ORGANIZATION_DTO,
} from '__tests__/data';
import { fullHeightDecorator, configureStory } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { mockSessionsPage as setupMocks } from '__tests__/mocks';

import { SessionsPage } from './SessionsPage';

export default {
  title: 'sessions/pages/SessionsPage',
  component: SessionsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const NoSessions = () => {
  return (
    <SessionsPage
      user={REBROWSE_ADMIN_DTO}
      sessions={[]}
      sessionCount={0}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
NoSessions.story = configureStory({
  setupMocks: (sandbox) => setupMocks(sandbox, { sessions: [] }),
});

export const WithSessions = () => {
  return (
    <SessionsPage
      user={REBROWSE_ADMIN_DTO}
      sessions={REBROWSE_SESSIONS_DTOS}
      sessionCount={REBROWSE_SESSIONS.length}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
WithSessions.story = configureStory({ setupMocks });
