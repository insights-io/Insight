import React from 'react';
import { INSIGHT_ADMIN, INSIGHT_SESSIONS } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';

import HomePage from './HomePage';

export default {
  title: 'app|pages/HomePage',
  decorators: [fullHeightDecorator],
};

export const NoSessions = () => {
  return <HomePage user={INSIGHT_ADMIN} sessions={[]} />;
};

export const WithSessions = () => {
  return <HomePage user={INSIGHT_ADMIN} sessions={INSIGHT_SESSIONS} />;
};
