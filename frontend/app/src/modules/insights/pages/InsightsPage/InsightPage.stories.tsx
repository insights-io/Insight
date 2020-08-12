import React from 'react';
import { INSIGHT_ADMIN } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';

import InsightsPage from './InsightsPage';

export default {
  title: 'insights/pages/InsightsPage',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <InsightsPage
      user={INSIGHT_ADMIN}
      countByLocation={[
        {
          count: 1,
          'location.countryName': 'Canada',
          'location.continentName': 'North America',
        },
        {
          count: 1,
          'location.countryName': 'Croatia',
          'location.continentName': 'Europe',
        },
        {
          count: 2,
          'location.countryName': 'Slovenia',
          'location.continentName': 'Europe',
        },
        {
          count: 1,
          'location.countryName': 'United States',
          'location.continentName': 'North America',
        },
      ]}
      countByDeviceClass={{ Phone: 10, Desktop: 20 }}
    />
  );
};
