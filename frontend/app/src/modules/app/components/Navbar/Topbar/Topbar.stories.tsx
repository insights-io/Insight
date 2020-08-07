import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { action } from '@storybook/addon-actions';

import Topbar from './Topbar';

export default {
  title: 'app|components/Topbar',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <Topbar onMenuClick={action('onMenuClick')} sidebarVisible={false} />;
};

export const SidebarVisible = () => {
  return <Topbar onMenuClick={action('onMenuClick')} sidebarVisible />;
};
