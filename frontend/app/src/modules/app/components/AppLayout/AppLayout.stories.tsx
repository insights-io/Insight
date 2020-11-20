import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { Block } from 'baseui/block';
import { INSIGHT_ADMIN } from 'test/data';
import type { Meta } from '@storybook/react';
import { INSIGHT_ORGANIZATION } from 'test/data/organization';

import { AppLayout } from './AppLayout';

export default {
  title: 'App/components/AppLayout',
  component: AppLayout,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <AppLayout user={INSIGHT_ADMIN} organization={INSIGHT_ORGANIZATION}>
      <Block display="flex" justifyContent="center">
        Some content
      </Block>
    </AppLayout>
  );
};
