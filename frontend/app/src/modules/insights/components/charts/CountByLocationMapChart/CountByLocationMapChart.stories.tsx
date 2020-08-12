import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';

import CountByLocationMapChart from './CountByLocationMapChart';

export default {
  title: 'insights/components/CountByLocationMapChart',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <CountByLocationMapChart
      data={[
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
    />
  );
};
