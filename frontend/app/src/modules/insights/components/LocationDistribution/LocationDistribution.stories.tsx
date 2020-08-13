import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';

import LocationDistribution from './LocationDistribution';

export default {
  title: 'insights/components/LocationDistribution',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <LocationDistribution
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
    />
  );
};
