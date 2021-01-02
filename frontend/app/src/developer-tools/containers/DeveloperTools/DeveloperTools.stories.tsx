import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { REBROWSE_EVENTS } from '__tests__/data';
import noop from 'lodash/noop';
import { filterBrowserEvent } from '__tests__/mocks/filter';
import type { Meta } from '@storybook/react';
import { httpOkResponse } from '__tests__/utils/request';
import { client } from 'sdk';

import { DeveloperTools } from './DeveloperTools';

export default {
  title: 'developer-tools/containers/DeveloperTools',
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
        .stub(client.recording.events, 'search')
        .callsFake((_sessionId, args = {}) => {
          return Promise.resolve(
            httpOkResponse(
              REBROWSE_EVENTS.filter((event) =>
                filterBrowserEvent(event, args.search)
              )
            )
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
      .stub(client.recording.events, 'search')
      .callsFake(() => new Promise(noop));
  },
});
