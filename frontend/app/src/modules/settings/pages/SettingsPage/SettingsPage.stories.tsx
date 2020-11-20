import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { REBROWSE_ADMIN_DTO, REBROWSE_ORGANIZATION_DTO } from 'test/data';

import { SettingsPage } from './SettingsPage';

export default {
  title: 'settings/pages/SettingsPage',
  component: SettingsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <SettingsPage
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
