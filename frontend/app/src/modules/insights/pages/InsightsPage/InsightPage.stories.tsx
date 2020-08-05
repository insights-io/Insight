import React from 'react';
import { INSIGHT_ADMIN } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';

import InsightsPage from './InsightsPage';

export default {
  title: 'insights|pages/InsightsPage',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <InsightsPage
      user={INSIGHT_ADMIN}
      countByCountry={{ Slovenia: 1, Croatia: 5, Hungary: 3, Germany: 4 }}
      countByDeviceClass={{ Phone: 10, Desktop: 20 }}
    />
  );
};
