import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { Block } from 'baseui/block';
import { REBROWSE_ADMIN, REBROWSE_ORGANIZATION } from '__tests__/data';
import type { Meta } from '@storybook/react';

import { AppLayout } from './AppLayout';

export default {
  title: 'App/components/AppLayout',
  component: AppLayout,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <AppLayout user={REBROWSE_ADMIN} organization={REBROWSE_ORGANIZATION}>
      <Block display="flex" justifyContent="center">
        Some content
      </Block>
    </AppLayout>
  );
};
