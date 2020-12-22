import React from 'react';
import { FETCH_EVENTS } from '__tests__/data';
import { fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';

import NetworkTab from './NetworkTab';

export default {
  title: 'sessions/components/NetworkTab',
  component: NetworkTab,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return <NetworkTab events={FETCH_EVENTS} loading={false} />;
};

export const Loading = () => {
  return <NetworkTab events={[]} loading />;
};
