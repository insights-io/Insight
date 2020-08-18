import React from 'react';
import { INSIGHT_ADMIN, INSIGHT_SESSIONS } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';

import SessionsPage from './SessionsPage';

export default {
  title: 'sessions/pages/SessionsPage',
  decorators: [fullHeightDecorator],
};

export const NoSessions = () => {
  return <SessionsPage user={INSIGHT_ADMIN} sessions={[]} sessionCount={0} />;
};

export const WithSessions = () => {
  return (
    <SessionsPage
      user={INSIGHT_ADMIN}
      sessions={INSIGHT_SESSIONS}
      sessionCount={INSIGHT_SESSIONS.length}
    />
  );
};
