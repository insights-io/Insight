import React from 'react';
import { configureStory } from '@rebrowse/storybook';
import { REBROWSE_SESSIONS, REBROWSE_EVENTS } from '__tests__/data';
import type { Meta } from '@storybook/react';
import { filterBrowserEvent } from '__tests__/mocks/filter';
import { httpOkResponse } from '__tests__/utils/request';
import { client } from 'sdk';

import { SessionDetails } from './SessionDetails';

export default {
  title: 'sessions/components/SessionDetails',
  component: SessionDetails,
} as Meta;

export const Base = () => {
  return <SessionDetails session={REBROWSE_SESSIONS[0]} />;
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
