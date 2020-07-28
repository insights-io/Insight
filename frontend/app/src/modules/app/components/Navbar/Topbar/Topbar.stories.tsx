import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';

import Topbar from './Topbar';

export default {
  title: 'app|components/Topbar',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <Topbar />;
};
