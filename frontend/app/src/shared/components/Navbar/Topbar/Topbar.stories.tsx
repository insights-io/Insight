import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { action } from '@storybook/addon-actions';
import type { Meta } from '@storybook/react';

import { NavbarTopbar, Props } from './Topbar';

export default {
  title: 'shared/components/NavbarTopbar',
  component: NavbarTopbar,
  decorators: [fullHeightDecorator],
} as Meta;

export const Controlled = (props: Pick<Props, 'sidebarVisible'>) => {
  return <NavbarTopbar onMenuClick={action('onMenuClick')} {...props} />;
};
Controlled.args = { sidebarVisible: true };

export const SidebarHidden = () => {
  return <Controlled sidebarVisible={false} />;
};

export const SidebarVisible = () => {
  return <Controlled sidebarVisible />;
};
