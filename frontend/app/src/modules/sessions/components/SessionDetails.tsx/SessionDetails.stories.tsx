import React from 'react';
import { configureStory } from '@rebrowse/storybook';
import { SessionApi } from 'api/session';
import { REBROWSE_SESSIONS, REBROWSE_EVENTS } from '__tests__/data';
import type { Meta } from '@storybook/react';
import { filterBrowserEvent } from '__tests__/mocks/filter';

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
        .stub(SessionApi.events, 'search')
        .callsFake((_sessionId, args = {}) => {
          return Promise.resolve(
            REBROWSE_EVENTS.filter((e) => filterBrowserEvent(e, args.search))
          );
        }),
    };
  },
});
