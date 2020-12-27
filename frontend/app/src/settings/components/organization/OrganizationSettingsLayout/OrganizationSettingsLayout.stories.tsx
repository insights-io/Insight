import React from 'react';
import { Meta } from '@storybook/react';
import { fullHeightDecorator } from '@rebrowse/storybook';

import { OrganizationSettingsLayout } from './OrganizationSettingsLayout';

export default {
  title: 'settings/components/OrganizationSettingsLayout',
  component: OrganizationSettingsLayout,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <OrganizationSettingsLayout
      header="Header"
      path={[
        { segment: 'settings', text: 'Settings' },
        { segment: 'organization', text: 'Organization' },
        { segment: 'auth', text: 'Auth' },
      ]}
    >
      <div style={{ height: '2000px', background: 'red' }}>
        Super duper content
      </div>
    </OrganizationSettingsLayout>
  );
};
