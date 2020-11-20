/* eslint-disable lodash/prefer-constant */
import React from 'react';
import { Meta } from '@storybook/react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { INSIGHT_SESSIONS } from 'test/data';

import { SessionList } from './SessionList';

export default {
  title: 'sessions/components/SessionList',
  component: SessionList,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <SessionList
      sessions={INSIGHT_SESSIONS}
      count={INSIGHT_SESSIONS.length}
      isItemLoaded={(_index) => true}
      loadMoreItems={() => Promise.resolve()}
    />
  );
};
