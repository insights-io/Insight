import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';

import AppLayout from './AppLayout';

export default {
  title: 'App|components/AppLayout',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <AppLayout>Some content</AppLayout>;
};
