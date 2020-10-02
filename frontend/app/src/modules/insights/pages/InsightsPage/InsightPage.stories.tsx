import React from 'react';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';
import { COUNT_BY_LOCATION, COUNT_BY_DEVICE } from 'test/data/sessions';
import { Meta } from '@storybook/react';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

import { InsightsPage } from './InsightsPage';

export default {
  title: 'insights/pages/InsightsPage',
  component: InsightsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <InsightsPage
      user={INSIGHT_ADMIN_DTO}
      countByLocation={COUNT_BY_LOCATION}
      organization={INSIGHT_ORGANIZATION_DTO}
      countByDeviceClass={COUNT_BY_DEVICE}
    />
  );
};
