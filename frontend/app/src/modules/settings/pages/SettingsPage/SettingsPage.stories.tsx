import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import type { Meta } from '@storybook/react';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

import { SettingsPage } from './SettingsPage';

export default {
  title: 'settings/pages/SettingsPage',
  component: SettingsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <SettingsPage
      user={INSIGHT_ADMIN_DTO}
      organization={INSIGHT_ORGANIZATION_DTO}
    />
  );
};
