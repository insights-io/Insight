import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { INSIGHT_SESSIONS } from 'test/data';

import SessionList from './SessionList';

export default {
  title: 'sessions/components/SessionList',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <SessionList sessions={INSIGHT_SESSIONS} />;
};
