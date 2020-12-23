import React from 'react';
import {
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_SESSIONS_PHONE_NO_LOCATION,
  REBROWSE_ADMIN_DTO,
  REBROWSE_ORGANIZATION_DTO,
} from '__tests__/data';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { AuthApi, SessionApi } from 'api';
import { Meta } from '@storybook/react';
import { SinonSandbox } from 'sinon';
import {
  retrieveSessionMockImplementation,
  searchEventsMockImplementation,
} from '__tests__/mocks/filter';

import { SessionPage } from './SessionPage';

export default {
  title: 'sessions/pages/SessionPage',
  component: SessionPage,
  decorators: [fullHeightDecorator],
} as Meta;

const setupMocks = (sandbox: SinonSandbox) => {
  return {
    retrieveUser: sandbox.stub(AuthApi.user, 'me').resolves(REBROWSE_ADMIN_DTO),

    retrieveOrganization: sandbox
      .stub(AuthApi.organization, 'get')
      .resolves(REBROWSE_ORGANIZATION_DTO),

    retrieveSessionStub: sandbox
      .stub(SessionApi, 'getSession')
      .callsFake((id) => retrieveSessionMockImplementation(id)),

    searchEvents: sandbox
      .stub(SessionApi.events, 'search')
      .callsFake((_sessionId, args = {}) =>
        searchEventsMockImplementation(args.search)
      ),
  };
};

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
Base.story = configureStory({ setupMocks });

export const NoLocation = () => {
  return (
    <SessionPage
      sessionId={REBROWSE_SESSIONS_PHONE_NO_LOCATION[0].id}
      session={REBROWSE_SESSIONS_PHONE_NO_LOCATION[0]}
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
Base.story = configureStory({ setupMocks });
