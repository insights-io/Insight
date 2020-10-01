import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { Block } from 'baseui/block';
import { INSIGHT_ADMIN } from 'test/data';

import AppLayout from './AppLayout';

export default {
  title: 'App/components/AppLayout',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return (
    <AppLayout user={INSIGHT_ADMIN}>
      <Block display="flex" justifyContent="center">
        Some content
      </Block>
    </AppLayout>
  );
};
