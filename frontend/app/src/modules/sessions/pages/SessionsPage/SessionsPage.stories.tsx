import React from 'react';
import {
  REBROWSE_ADMIN_DTO,
  REBROWSE_SESSIONS,
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_ORGANIZATION_DTO,
} from '__tests__/data';
import { fullHeightDecorator, configureStory } from '@rebrowse/storybook';
import { AuthApi, SessionApi } from 'api';
import get from 'lodash/get';
import type { Meta } from '@storybook/react';
import { countSessionsBy, filterSession } from '__tests__/mocks/filter';

import { SessionsPage } from './SessionsPage';

export default {
  title: 'sessions/pages/SessionsPage',
  component: SessionsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const NoSessions = () => {
  return (
    <SessionsPage
      user={REBROWSE_ADMIN_DTO}
      sessions={[]}
      sessionCount={0}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
NoSessions.story = configureStory({
  setupMocks: (sandbox) => {
    const retrieveUserStub = sandbox
      .stub(AuthApi.user, 'me')
      .resolves(REBROWSE_ADMIN_DTO);

    const retrieveOrganizationStub = sandbox
      .stub(AuthApi.organization, 'get')
      .resolves(REBROWSE_ORGANIZATION_DTO);

    return { retrieveUserStub, retrieveOrganizationStub };
  },
});

export const WithSessions = () => {
  return (
    <SessionsPage
      user={REBROWSE_ADMIN_DTO}
      sessions={REBROWSE_SESSIONS_DTOS}
      sessionCount={REBROWSE_SESSIONS.length}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
WithSessions.story = configureStory({
  setupMocks: (sandbox) => {
    const getDistinctStub = sandbox
      .stub(SessionApi, 'distinct')
      .callsFake((on: string) => {
        return Promise.resolve([
          ...new Set(
            REBROWSE_SESSIONS_DTOS.map((s) => get(s, on)).filter(Boolean)
          ),
        ]);
      });

    const getSessionsStub = sandbox
      .stub(SessionApi, 'getSessions')
      .callsFake((args = {}) => {
        return Promise.resolve(
          REBROWSE_SESSIONS_DTOS.filter((s) => filterSession(s, args.search))
        );
      });

    const countSessionsStub = sandbox
      .stub(SessionApi, 'count')
      .callsFake((args = {}) => {
        return Promise.resolve(
          countSessionsBy(REBROWSE_SESSIONS_DTOS, args.search)
        );
      });
    const retrieveUserStub = sandbox
      .stub(AuthApi.user, 'me')
      .resolves(REBROWSE_ADMIN_DTO);

    const retrieveOrganizationStub = sandbox
      .stub(AuthApi.organization, 'get')
      .resolves(REBROWSE_ORGANIZATION_DTO);

    return {
      getSessionsStub,
      countSessionsStub,
      getDistinctStub,
      retrieveUserStub,
      retrieveOrganizationStub,
    };
  },
});
