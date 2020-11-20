import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import type { Meta } from '@storybook/react';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

import { AccountSettingsDetailsPage } from './AccountSettingsDetailsPage';

export default {
  title: 'settings/pages/account/AccountSettingsDetailsPage',
  component: AccountSettingsDetailsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <AccountSettingsDetailsPage
      user={INSIGHT_ADMIN_DTO}
      organization={INSIGHT_ORGANIZATION_DTO}
    />
  );
};
