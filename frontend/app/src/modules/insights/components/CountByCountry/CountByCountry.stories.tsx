import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { GROUP_BY_COUNTRY } from 'test/data/sessions';

import CountByCountry from './CountByCountry';

export default {
  title: 'insights/components/CountByCountry',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <CountByCountry data={GROUP_BY_COUNTRY} />;
};
