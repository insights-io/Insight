import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { Meta } from '@storybook/react';
import { REBROWSE_ORGANIZATION_DTO, REBROWSE_ADMIN_DTO } from '__tests__/data';
import { mockAcocuntSettingsAuthTokensPage as setupMocks } from '__tests__/mocks';
import { AUTH_TOKEN_DTO } from '__tests__/data/sso';

import { AccountSettingsAuthTokensPage } from './AccountSettingsAuthTokensPage';

export default {
  title: 'settings/pages/account/AccountSettingsAuthTokensPage',
  component: AccountSettingsAuthTokensPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <AccountSettingsAuthTokensPage
      user={REBROWSE_ADMIN_DTO}
      authTokens={[AUTH_TOKEN_DTO]}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
Base.story = configureStory({ setupMocks });
