import React from 'react';
import { configureStory } from '@rebrowse/storybook';
import { AuthApi } from 'api/auth';
import {
  REBROWSE_ADMIN,
  SMS_MFA_SETUP_DTO,
  TFA_SETUP_QR_IMAGE,
  TOTP_MFA_SETUP_DTO,
} from 'test/data';
import type { Meta } from '@storybook/react';

import { AccountMfaPanel } from './AccountMfaPanel';

export default {
  title: 'settings/components/account/AccountMfaPanel',
  component: AccountMfaPanel,
} as Meta;

export const TfaEnabled = () => {
  return (
    <AccountMfaPanel
      user={REBROWSE_ADMIN}
      mfaSetups={[TOTP_MFA_SETUP_DTO, SMS_MFA_SETUP_DTO]}
    />
  );
};
TfaEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox
        .stub(AuthApi.mfa.setup, 'list')
        .resolves([TOTP_MFA_SETUP_DTO]),

      setupStart: sandbox.stub(AuthApi.mfa.setup.totp, 'start').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),

      setupComplete: sandbox
        .stub(AuthApi.mfa.setup, 'complete')
        .resolves(TOTP_MFA_SETUP_DTO),

      setupSendSmsCode: sandbox
        .stub(AuthApi.mfa.setup.sms, 'sendCode')
        .resolves({ validitySeconds: 60 }),
    };
  },
});
