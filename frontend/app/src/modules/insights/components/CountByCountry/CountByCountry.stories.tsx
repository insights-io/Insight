import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';

import CountByCountry from './CountByCountry';

export default {
  title: 'insights|components/CountByCountry',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <CountByCountry data={{ Slovenia: 1, Crotia: 5, Hungary: 3, Germany: 4 }} />
  );
};
