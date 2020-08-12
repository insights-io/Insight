import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { action } from '@storybook/addon-actions';

import Topbar, { Props } from './Topbar';

export default {
  title: 'app/components/Topbar',
  decorators: [fullHeightDecorator],
};

export const Controlled = (props: Partial<Props>) => {
  return <Topbar onMenuClick={action('onMenuClick')} {...props} />;
};
Controlled.args = { sidebarVisible: true };

export const SidebarHidden = () => {
  return <Controlled sidebarVisible={false} />;
};

export const SidebarVisible = () => {
  return <Controlled sidebarVisible />;
};
