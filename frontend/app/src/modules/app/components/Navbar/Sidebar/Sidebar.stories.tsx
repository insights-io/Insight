import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import useSidebar from 'modules/app/hooks/useSidebar';
import { INSIGHT_ADMIN } from 'test/data';
import type { Meta } from '@storybook/react';
import { INSIGHT_ORGANIZATION } from 'test/data/organization';

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
      user={INSIGHT_ADMIN}
      organization={INSIGHT_ORGANIZATION}
    />
  );
};
