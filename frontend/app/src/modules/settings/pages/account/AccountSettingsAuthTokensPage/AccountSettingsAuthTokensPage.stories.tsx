import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { Meta } from '@storybook/react';
import { AuthTokenDTO } from '@rebrowse/types';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

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
  return (
    <AccountSettingsAuthTokensPage
      user={INSIGHT_ADMIN_DTO}
      authTokens={[AUTH_TOKEN_DTO]}
      organization={INSIGHT_ORGANIZATION_DTO}
    />
  );
};
