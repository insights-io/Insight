import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { SessionApi } from 'api';
import { REBROWSE_EVENTS } from 'test/data';
import noop from 'lodash/noop';
import { filterBrowserEvent } from 'test/mocks/filter';
import type { Meta } from '@storybook/react';

import DevTools from './DevTools';

export default {
  title: 'sessions/containers/DevTools',
  component: DevTools,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return <DevTools sessionId="10" />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      searchEvents: sandbox
        .stub(SessionApi.events, 'search')
        .callsFake((_sessionId, args = {}) => {
          return Promise.resolve(
            REBROWSE_EVENTS.filter((e) => filterBrowserEvent(e, args.search))
          );
        }),
    };
  },
});

export const Loading = () => {
  return <DevTools sessionId="10" />;
};
Loading.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'search')
      .callsFake(() => new Promise(noop));
  },
});
