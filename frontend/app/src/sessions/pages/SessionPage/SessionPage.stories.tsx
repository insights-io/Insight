import React from 'react';
import {
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_ADMIN_DTO,
  REBROWSE_ORGANIZATION_DTO,
  REBROWSE_EVENTS,
} from '__tests__/data';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { AuthApi, SessionApi } from 'api';
import { Meta } from '@storybook/react';
import { filterBrowserEvent } from '__tests__/mocks/filter';

import { SessionPage } from './SessionPage';

export default {
  title: 'sessions/pages/SessionPage',
  component: SessionPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <SessionPage
      sessionId={REBROWSE_SESSIONS_DTOS[0].id}
      session={REBROWSE_SESSIONS_DTOS[0]}
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      retrieveUser: sandbox
        .stub(AuthApi.user, 'me')
        .resolves(REBROWSE_ADMIN_DTO),

      retrieveOrganization: sandbox
        .stub(AuthApi.organization, 'get')
        .resolves(REBROWSE_ORGANIZATION_DTO),

      getSessions: sandbox
        .stub(SessionApi, 'getSession')
        .resolves(REBROWSE_SESSIONS_DTOS[0]),

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
