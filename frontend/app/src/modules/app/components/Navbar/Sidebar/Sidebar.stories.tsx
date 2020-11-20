import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import useSidebar from 'modules/app/hooks/useSidebar';
import { REBROWSE_ADMIN } from 'test/data';
import { REBROWSE_ORGANIZATION } from 'test/data/organization';
import type { Meta } from '@storybook/react';

import { Sidebar } from './Sidebar';

export default {
  title: 'app/components/Sidebar',
  component: Sidebar,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <Sidebar
      {...useSidebar()}
      user={REBROWSE_ADMIN}
      organization={REBROWSE_ORGANIZATION}
    />
  );
};
