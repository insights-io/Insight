import React from 'react';
import { REBROWSE_ADMIN_DTO, REBROWSE_ORGANIZATION_DTO } from '__tests__/data';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import {
  COUNT_SESSIONS_BY_DATE,
  COUNT_SESSIONS_BY_DEVICE_CLASS,
  COUNT_PAGE_VISITS_BY_DATE,
  COUNT_SESSIONS_BY_LOCATION,
} from '__tests__/data/sessions';
import type { Meta } from '@storybook/react';
import { mockIndexPage as setupMocks } from '__tests__/mocks';

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
      organization={REBROWSE_ORGANIZATION_DTO}
      sessionsByLocationCount={COUNT_SESSIONS_BY_LOCATION}
      sessionsByDeviceCount={COUNT_SESSIONS_BY_DEVICE_CLASS}
      sessionsByDateCount={COUNT_SESSIONS_BY_DATE}
      pageVisitsByDateCount={COUNT_PAGE_VISITS_BY_DATE}
      relativeTimeRange="30d"
    />
  );
};
Base.story = configureStory({ setupMocks });

export const Empty = () => {
  return (
    <InsightsPage
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
      sessionsByLocationCount={[]}
      sessionsByDeviceCount={[]}
      sessionsByDateCount={[]}
      pageVisitsByDateCount={[]}
      relativeTimeRange="30d"
    />
  );
};
Empty.story = configureStory({
  setupMocks: (sandbox) => setupMocks(sandbox, { sessions: [] }),
});
