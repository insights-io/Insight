/* eslint-disable lodash/prefer-constant */
import React from 'react';
import { Meta } from '@storybook/react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { REBROWSE_SESSIONS } from '__tests__/data';

import { SessionList } from './SessionList';

export default {
  title: 'sessions/components/SessionList',
  component: SessionList,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <SessionList
      sessions={REBROWSE_SESSIONS}
      count={REBROWSE_SESSIONS.length}
      isItemLoaded={(_index) => true}
      loadMoreItems={() => Promise.resolve()}
    />
  );
};
