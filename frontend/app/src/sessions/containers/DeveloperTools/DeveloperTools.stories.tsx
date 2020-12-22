import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { SessionApi } from 'api';
import { REBROWSE_EVENTS } from '__tests__/data';
import noop from 'lodash/noop';
import { filterBrowserEvent } from '__tests__/mocks/filter';
import type { Meta } from '@storybook/react';

import { DeveloperTools } from './DeveloperTools';

export default {
  title: 'sessions/containers/DeveloperTools',
  component: DeveloperTools,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <DeveloperTools sessionId="10">
      {(open) => <DeveloperTools.Trigger open={open} />}
    </DeveloperTools>
  );
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
  return (
    <DeveloperTools sessionId="10">
      {(open) => <DeveloperTools.Trigger open={open} />}
    </DeveloperTools>
  );
};
Loading.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SessionApi.events, 'search')
      .callsFake(() => new Promise(noop));
  },
});
