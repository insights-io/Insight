import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import {
  SMS_MFA_SETUP_DTO,
  TOTP_MFA_SETUP_QR_IMAGE,
  TOTP_MFA_SETUP_DTO,
} from 'test/data/mfa';
import { AuthApi } from 'api';
import type { Meta } from '@storybook/react';
import type { ResponsePromise } from 'ky';
import { REBROWSE_ADMIN_DTO } from 'test/data/user';
import { REBROWSE_ORGANIZATION_DTO } from 'test/data/organization';

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
      listSetups: sandbox.stub(AuthApi.mfa.setup, 'list').resolves(list),

      setupStart: sandbox.stub(AuthApi.mfa.setup.totp, 'start').resolves({
        data: { qrImage: TOTP_MFA_SETUP_QR_IMAGE },
      }),

      setupComplete: sandbox
        .stub(AuthApi.mfa.setup, 'complete')
        .resolves(TOTP_MFA_SETUP_DTO),

      disable: sandbox
        .stub(AuthApi.mfa.setup, 'disable')
        .callsFake((method) => {
          list = list.filter((s) => s.method !== method);
          return {} as ResponsePromise;
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
      listSetups: sandbox.stub(AuthApi.mfa.setup, 'list').resolves([]),
      setupStart: sandbox.stub(AuthApi.mfa.setup.totp, 'start').resolves({
        data: { qrImage: TOTP_MFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox
        .stub(AuthApi.mfa.setup, 'complete')
        .resolves(TOTP_MFA_SETUP_DTO),
    };
  },
});
