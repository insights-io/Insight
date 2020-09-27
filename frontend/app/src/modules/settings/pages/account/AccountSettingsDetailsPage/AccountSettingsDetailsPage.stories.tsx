import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import type { Meta } from '@storybook/react';

import { AccountSettingsDetailsPage } from './AccountSettingsDetailsPage';

export default {
  title: 'settings/pages/account/AccountSettingsDetailsPage',
  component: AccountSettingsDetailsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return <AccountSettingsDetailsPage user={INSIGHT_ADMIN_DTO} />;
};
