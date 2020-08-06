import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';

import CountByLocationMapChart from './CountByLocationMapChart';

export default {
  title: 'insights|components/CountByLocationMapChart',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <CountByLocationMapChart
      data={{
        'North America': [
          { 'location.countryName': 'United Stated', count: 2 },
        ],
        Europe: [
          { 'location.countryName': 'Slovenia', count: 2 },
          { 'location.countryName': 'Croatia', count: 1 },
        ],
      }}
    />
  );
};
