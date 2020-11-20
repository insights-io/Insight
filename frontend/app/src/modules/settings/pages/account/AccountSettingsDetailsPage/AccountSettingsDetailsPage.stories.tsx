import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { REBROWSE_ORGANIZATION_DTO, REBROWSE_ADMIN_DTO } from 'test/data';
import type { Meta } from '@storybook/react';

import { AccountSettingsDetailsPage } from './AccountSettingsDetailsPage';

export default {
  title: 'settings/pages/account/AccountSettingsDetailsPage',
  component: AccountSettingsDetailsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <AccountSettingsDetailsPage
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
