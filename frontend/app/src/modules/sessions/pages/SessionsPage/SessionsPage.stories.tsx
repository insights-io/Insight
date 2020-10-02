import React from 'react';
import {
  INSIGHT_ADMIN_DTO,
  INSIGHT_SESSIONS,
  INSIGHT_SESSIONS_DTOS,
} from 'test/data';
import { fullHeightDecorator, configureStory } from '@insight/storybook';
import { SessionApi } from 'api';
import type { SessionDTO } from '@insight/types';
import { SessionSearchBean } from '@insight/sdk/dist/sessions';
import get from 'lodash/get';
import type { Meta } from '@storybook/react';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

import { SessionsPage } from './SessionsPage';

export default {
  title: 'sessions/pages/SessionsPage',
  component: SessionsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const NoSessions = () => {
  return (
    <SessionsPage
      user={INSIGHT_ADMIN_DTO}
      sessions={[]}
      sessionCount={0}
      organization={INSIGHT_ORGANIZATION_DTO}
    />
  );
};

export const WithSessions = () => {
  return (
    <SessionsPage
      user={INSIGHT_ADMIN_DTO}
      sessions={INSIGHT_SESSIONS_DTOS}
      sessionCount={INSIGHT_SESSIONS.length}
      organization={INSIGHT_ORGANIZATION_DTO}
    />
  );
};
WithSessions.story = configureStory({
  setupMocks: (sandbox) => {
    const filter = (s: SessionDTO, search: SessionSearchBean | undefined) => {
      if (!search) {
        return true;
      }

      const {
        location: { city, countryName, continentName },
      } = s;

      if (search['location.city']) {
        return `eq:${city}` === search['location.city'];
      }
      if (search['location.countryName']) {
        return `eq:${countryName}` === search['location.countryName'];
      }

      if (search['location.continentName']) {
        return `eq:${continentName}` === search['location.continentName'];
      }

      return true;
    };

    const getDistinctStub = sandbox
      .stub(SessionApi, 'distinct')
      .callsFake((on: string) => {
        return Promise.resolve([
          ...new Set(
            INSIGHT_SESSIONS_DTOS.map((s) => get(s, on)).filter(Boolean)
          ),
        ]);
      });

    const getSessionsStub = sandbox
      .stub(SessionApi, 'getSessions')
      .callsFake((args = {}) => {
        return Promise.resolve(
          INSIGHT_SESSIONS_DTOS.filter((s) => filter(s, args.search))
        );
      });

    const getSessionCountStub = sandbox
      .stub(SessionApi, 'count')
      .callsFake((args = {}) => {
        return Promise.resolve({
          count: INSIGHT_SESSIONS_DTOS.filter((s) => filter(s, args.search))
            .length,
        });
      });

    return { getSessionsStub, getSessionCountStub, getDistinctStub };
  },
});
