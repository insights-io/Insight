import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import {
  SMS_MFA_SETUP_DTO,
  TOTP_MFA_SETUP_QR_IMAGE,
  TOTP_MFA_SETUP_DTO,
} from '__tests__/data/mfa';
import { AuthApi } from 'api';
import type { Meta } from '@storybook/react';
import { REBROWSE_ADMIN_DTO } from '__tests__/data/user';
import { REBROWSE_ORGANIZATION_DTO } from '__tests__/data/organization';

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
MfaEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    let list = [TOTP_MFA_SETUP_DTO, SMS_MFA_SETUP_DTO];

    return {
      listSetups: sandbox.stub(AuthApi.mfa.setup, 'list').resolves({
        data: { data: list },
        statusCode: 200,
        headers: new Headers(),
      }),

      setupStart: sandbox.stub(AuthApi.mfa.setup.totp, 'start').resolves({
        data: { data: { qrImage: TOTP_MFA_SETUP_QR_IMAGE } },
        statusCode: 200,
        headers: new Headers(),
      }),

      setupComplete: sandbox.stub(AuthApi.mfa.setup, 'complete').resolves({
        data: { data: TOTP_MFA_SETUP_DTO },
        statusCode: 200,
        headers: new Headers(),
      }),

      disable: sandbox
        .stub(AuthApi.mfa.setup, 'disable')
        .callsFake((method) => {
          list = list.filter((s) => s.method !== method);
          return Promise.resolve({ statusCode: 200, headers: new Headers() });
        }),
    };
  },
});

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
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox.stub(AuthApi.mfa.setup, 'list').resolves({
        data: { data: [] },
        statusCode: 200,
        headers: new Headers(),
      }),
      setupStart: sandbox.stub(AuthApi.mfa.setup.totp, 'start').resolves({
        data: { data: { qrImage: TOTP_MFA_SETUP_QR_IMAGE } },
        statusCode: 200,
        headers: new Headers(),
      }),
      setupComplete: sandbox.stub(AuthApi.mfa.setup, 'complete').resolves({
        data: { data: TOTP_MFA_SETUP_DTO },
        statusCode: 200,
        headers: new Headers(),
      }),
    };
  },
});
