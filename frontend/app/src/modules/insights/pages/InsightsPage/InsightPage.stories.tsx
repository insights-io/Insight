import React from 'react';
import { INSIGHT_ADMIN } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';
import { COUNT_BY_LOCATION, COUNT_BY_DEVICE } from 'test/data/sessions';

import InsightsPage from './InsightsPage';

export default {
  title: 'insights/pages/InsightsPage',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <InsightsPage
      user={INSIGHT_ADMIN}
      countByLocation={COUNT_BY_LOCATION}
      countByDeviceClass={COUNT_BY_DEVICE}
    />
  );
};
