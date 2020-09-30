import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import useSidebar from 'modules/app/hooks/useSidebar';
import { INSIGHT_ADMIN } from 'test/data';

import Sidebar from './Sidebar';

export default {
  title: 'app/components/Sidebar',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <Sidebar {...useSidebar()} user={INSIGHT_ADMIN} />;
};
