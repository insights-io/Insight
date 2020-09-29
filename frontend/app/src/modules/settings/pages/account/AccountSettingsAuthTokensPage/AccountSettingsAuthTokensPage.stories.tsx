import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { Meta } from '@storybook/react';
import { AuthTokenDTO } from '@insight/types';

import { AccountSettingsAuthTokensPage } from './AccountSettingsAuthTokensPage';

export default {
  title: 'settings/pages/account/AccountSettingsAuthTokensPage',
  component: AccountSettingsAuthTokensPage,
  decorators: [fullHeightDecorator],
} as Meta;

const AUTH_TOKEN_DTO: AuthTokenDTO = {
  userId: '123',
  token: 'superToken',
  createdAt: new Date().toUTCString(),
};

export const Base = () => {
  return <AccountSettingsAuthTokensPage authTokens={[AUTH_TOKEN_DTO]} />;
};
