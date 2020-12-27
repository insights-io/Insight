import React from 'react';
import { configureStory } from '@rebrowse/storybook';
import { AuthApi } from 'api/auth';
import { REBROWSE_ADMIN } from '__tests__/data/user';
import {
  SMS_MFA_SETUP_DTO,
  TOTP_MFA_SETUP_QR_IMAGE,
  TOTP_MFA_SETUP_DTO,
} from '__tests__/data/mfa';
import type { Meta } from '@storybook/react';

import { AccountMfaPanel } from './AccountMfaPanel';

export default {
  title: 'settings/components/account/AccountMfaPanel',
  component: AccountMfaPanel,
} as Meta;

export const MfaEnabled = () => {
  return (
    <AccountMfaPanel
      user={REBROWSE_ADMIN}
      mfaSetups={[TOTP_MFA_SETUP_DTO, SMS_MFA_SETUP_DTO]}
    />
  );
};
MfaEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox
        .stub(AuthApi.mfa.setup, 'list')
        .resolves([TOTP_MFA_SETUP_DTO]),

      setupStart: sandbox.stub(AuthApi.mfa.setup.totp, 'start').resolves({
        data: { qrImage: TOTP_MFA_SETUP_QR_IMAGE },
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
