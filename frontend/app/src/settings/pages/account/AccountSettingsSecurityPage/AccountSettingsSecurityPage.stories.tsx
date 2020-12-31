import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { SMS_MFA_SETUP_DTO, TOTP_MFA_SETUP_DTO } from '__tests__/data/mfa';
import { REBROWSE_ADMIN_DTO } from '__tests__/data/user';
import { REBROWSE_ORGANIZATION_DTO } from '__tests__/data/organization';
import { mockAccountSettingsSecurityPage as setupMocks } from '__tests__/mocks';
import type { Meta } from '@storybook/react';

import { AccountSettingsSecurityPage } from './AccountSettingsSecurityPage';

export default {
  title: 'settings/pages/account/AccountSettingsSecurityPage',
  component: AccountSettingsSecurityPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const MfaEnabled = () => {
  return (
    <AccountSettingsSecurityPage
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
      mfaSetups={[TOTP_MFA_SETUP_DTO, SMS_MFA_SETUP_DTO]}
    />
  );
};
MfaEnabled.story = configureStory({ setupMocks });

export const MfaDisabled = () => {
  return (
    <AccountSettingsSecurityPage
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
      mfaSetups={[]}
    />
  );
};
MfaDisabled.story = configureStory({
  setupMocks: (sandbox) => setupMocks(sandbox, { mfaSetups: [] }),
});
