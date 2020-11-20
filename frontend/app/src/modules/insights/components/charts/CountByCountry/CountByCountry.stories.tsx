import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';

import CountByCountryChart from './CountByCountry';

export default {
  title: 'insights/components/CountByCountryChart',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <CountByCountryChart
      data={{ Slovenia: 1, Crotia: 5, Hungary: 3, Germany: 4 }}
    />
  );
};
