import React from 'react';
import { REBROWSE_ADMIN_DTO, REBROWSE_ORGANIZATION_DTO } from 'test/data';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import {
  COUNT_SESSIONS_BY_DATE,
  COUNT_SESSIONS_BY_DEVICE_CLASS,
  COUNT_PAGE_VISITS_BY_DATE,
  COUNT_SESSIONS_BY_LOCATION,
} from 'test/data/sessions';
import type { Meta } from '@storybook/react';
import { AuthApi } from 'api';

import { InsightsPage } from './InsightsPage';

export default {
  title: 'insights/pages/InsightsPage',
  component: InsightsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <InsightsPage
      user={REBROWSE_ADMIN_DTO}
      countSessionsByLocation={COUNT_SESSIONS_BY_LOCATION}
      organization={REBROWSE_ORGANIZATION_DTO}
      countSessionsByDeviceClass={COUNT_SESSIONS_BY_DEVICE_CLASS}
      countPageVisitsByDate={COUNT_PAGE_VISITS_BY_DATE}
      countSessionsByDate={COUNT_SESSIONS_BY_DATE}
    />
  );
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    const retrieveOrganization = sandbox
      .stub(AuthApi.organization, 'get')
      .resolves(REBROWSE_ORGANIZATION_DTO);

    const retrieveUser = sandbox
      .stub(AuthApi.user, 'me')
      .resolves(REBROWSE_ADMIN_DTO);

    return { retrieveOrganization, retrieveUser };
  },
});
