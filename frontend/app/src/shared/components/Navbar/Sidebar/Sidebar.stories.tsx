import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { REBROWSE_ADMIN } from '__tests__/data';
import { REBROWSE_ORGANIZATION } from '__tests__/data/organization';
import type { Meta } from '@storybook/react';
import useSidebar from 'shared/hooks/useSidebar';

import { Sidebar } from './Sidebar';

export default {
  title: 'shared/components/Sidebar',
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
