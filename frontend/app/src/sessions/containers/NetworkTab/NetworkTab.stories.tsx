import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { SessionApi } from 'api';
import { FETCH_EVENTS } from '__tests__/data';
import noop from 'lodash/noop';
import type { Meta } from '@storybook/react';

import { NetworkTabContainer } from './NetworkTab';

export default {
  title: 'sessions/containers/NetworkTabContainer',
  component: NetworkTabContainer,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return <NetworkTabContainer sessionId="10" />;
};

Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'search')
      .resolves([...Object.values(FETCH_EVENTS)]);
  },
});

export const Loading = () => {
  return <NetworkTabContainer sessionId="10" />;
};
Loading.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'search')
      .callsFake(() => new Promise(noop));
  },
});
