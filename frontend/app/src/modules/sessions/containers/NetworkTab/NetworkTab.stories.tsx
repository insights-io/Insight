import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { SessionApi } from 'api';
import { FETCH_EVENTS } from '__tests__/data';
import NetworkTab from 'modules/sessions/containers/NetworkTab';
import noop from 'lodash/noop';

export default {
  title: 'sessions/containers/NetworkTab',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <NetworkTab sessionId="10" />;
};

Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'search')
      .resolves([...Object.values(FETCH_EVENTS)]);
  },
});

export const Loading = () => {
  return <NetworkTab sessionId="10" />;
};
Loading.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'search')
      .callsFake(() => new Promise(noop));
  },
});
