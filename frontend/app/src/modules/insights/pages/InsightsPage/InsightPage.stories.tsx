import React from 'react';
import { REBROWSE_ADMIN_DTO, REBROWSE_ORGANIZATION_DTO } from 'test/data';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { COUNT_BY_LOCATION, COUNT_BY_DEVICE } from 'test/data/sessions';
import type { Meta } from '@storybook/react';

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
      countByLocation={COUNT_BY_LOCATION}
      organization={REBROWSE_ORGANIZATION_DTO}
      countByDeviceClass={COUNT_BY_DEVICE}
      countPageVisitsByDate={[]}
      countSessionsByDate={[]}
    />
  );
};
