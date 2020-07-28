import React from 'react';
import { SIDEBAR_WIDTH } from 'shared/theme';
import { fullHeightDecorator } from '@insight/storybook';

import Navbar from './Navbar';

export default {
  title: 'app|components/Navbar',
  decorators: [fullHeightDecorator],
};

export const Topbar = () => {
  return <Navbar type="topbar" />;
};

export const Sidebar = () => {
  return (
    <Navbar
      type="sidebar"
      overrides={{
        Root: {
          Block: {
            style: {
              height: '100%',
              width: SIDEBAR_WIDTH,
              flexDirection: 'column',
            },
          },
        },
      }}
    />
  );
};
