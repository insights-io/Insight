import React from 'react';
import { Meta } from '@storybook/react';
import { fullHeightDecorator } from '@rebrowse/storybook';

import { AccountSettingsLayout } from './AccountSettingsLayout';

export default {
  title: 'settings/components/AccountSettingsLayout',
  component: AccountSettingsLayout,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <AccountSettingsLayout
      header="Header"
      path={[
        { segment: 'settings', text: 'Settings' },
        { segment: 'user', text: 'User' },
        { segment: 'details', text: 'Details' },
      ]}
    >
      <div style={{ height: '2000px', background: 'red' }}>
        Super duper content
      </div>
    </AccountSettingsLayout>
  );
};
