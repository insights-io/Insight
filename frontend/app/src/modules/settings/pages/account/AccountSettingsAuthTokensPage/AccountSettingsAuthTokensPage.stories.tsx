import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { Meta } from '@storybook/react';

import { AccountSettingsAuthTokensPage } from './AccountSettingsAuthTokensPage';

export default {
  title: 'settings/pages/account/AccountSettingsAuthTokensPage',
  component: AccountSettingsAuthTokensPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return <AccountSettingsAuthTokensPage />;
};
