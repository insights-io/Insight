import React from 'react';
import { INSIGHT_ADMIN } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';
import { Meta } from '@storybook/react';
import { ORGANIZATION_GENERAL_SETTINGS_PAGE } from 'shared/constants/routes';

import AccountOrganizationSettingsPage from './AccountOrganizationSettingsPage';

export default {
  title: 'settings/pages/AccountOrganizationSettingsPage',
  component: AccountOrganizationSettingsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <AccountOrganizationSettingsPage
      user={INSIGHT_ADMIN}
      activeTab={ORGANIZATION_GENERAL_SETTINGS_PAGE}
    />
  );
};
