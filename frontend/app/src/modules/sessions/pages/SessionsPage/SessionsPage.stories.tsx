import React from 'react';
import { INSIGHT_ADMIN, INSIGHT_SESSIONS } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';

import SessionsPage from './SessionsPage';

export default {
  title: 'sessions/pages/SessionsPage',
  decorators: [fullHeightDecorator],
};

export const NoSessions = () => {
  return <SessionsPage user={INSIGHT_ADMIN} sessions={[]} />;
};

export const WithSessions = () => {
  return <SessionsPage user={INSIGHT_ADMIN} sessions={INSIGHT_SESSIONS} />;
};
