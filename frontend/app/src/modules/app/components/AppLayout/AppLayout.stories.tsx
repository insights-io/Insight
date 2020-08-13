import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { Block } from 'baseui/block';

import AppLayout from './AppLayout';

export default {
  title: 'App/components/AppLayout',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <AppLayout>
      <Block display="flex" justifyContent="center">
        Some content
      </Block>
    </AppLayout>
  );
};
