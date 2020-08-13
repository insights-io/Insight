import React from 'react';
import { FETCH_EVENTS } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';

import NetworkTab from './NetworkTab';

export default {
  title: 'sessions/components/NetworkTab',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <NetworkTab
      events={[
        FETCH_EVENTS.BEACON_BEAT_EVENT,
        FETCH_EVENTS.CREATE_PAGE_EVENT,
        FETCH_EVENTS.GET_SESSION_EVENT,
        FETCH_EVENTS.NEXT_STACK_FRAME_EVENT,
      ]}
      loading={false}
    />
  );
};

export const Loading = () => {
  return <NetworkTab events={[]} loading />;
};
