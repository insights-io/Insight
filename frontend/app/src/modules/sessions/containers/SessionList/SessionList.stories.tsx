import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { INSIGHT_SESSIONS } from 'test/data';
import { createDateRange } from 'modules/sessions/components/SessionSearch/utils';

import SessionList from './SessionList';

export default {
  title: 'sessions/components/SessionList',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <SessionList
      initialSessions={INSIGHT_SESSIONS}
      initialSessionCount={INSIGHT_SESSIONS.length}
      dateRange={createDateRange('all-time')}
      filters={[]}
    />
  );
};
