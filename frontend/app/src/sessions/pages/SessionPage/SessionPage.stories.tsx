import React from 'react';
import {
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_SESSIONS_PHONE_NO_LOCATION,
  REBROWSE_ADMIN_DTO,
  REBROWSE_ORGANIZATION_DTO,
} from '__tests__/data';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { mockSessionPage as setupMocks } from '__tests__/mocks';

import { SessionPage } from './SessionPage';

export default {
  title: 'sessions/pages/SessionPage',
  component: SessionPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <SessionPage
      sessionId={REBROWSE_SESSIONS_DTOS[0].id}
      session={REBROWSE_SESSIONS_DTOS[0]}
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
Base.story = configureStory({ setupMocks });

export const NoLocation = () => {
  return (
    <SessionPage
      sessionId={REBROWSE_SESSIONS_PHONE_NO_LOCATION[0].id}
      session={REBROWSE_SESSIONS_PHONE_NO_LOCATION[0]}
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
Base.story = configureStory({ setupMocks });
