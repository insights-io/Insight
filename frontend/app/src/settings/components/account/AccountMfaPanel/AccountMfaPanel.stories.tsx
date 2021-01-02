import React from 'react';
import { configureStory } from '@rebrowse/storybook';
import { REBROWSE_ADMIN } from '__tests__/data/user';
import {
  SMS_MFA_SETUP_DTO,
  TOTP_MFA_SETUP_QR_IMAGE,
  TOTP_MFA_SETUP_DTO,
} from '__tests__/data/mfa';
import type { Meta } from '@storybook/react';
import { httpOkResponse } from '__tests__/utils/request';
import { client } from 'sdk';

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
        .stub(client.auth.mfa.setup, 'list')
        .resolves(httpOkResponse([TOTP_MFA_SETUP_DTO])),

      setupStart: sandbox
        .stub(client.auth.mfa.setup.totp, 'start')
        .resolves(httpOkResponse({ qrImage: TOTP_MFA_SETUP_QR_IMAGE })),

      setupComplete: sandbox
        .stub(client.auth.mfa.setup, 'complete')
        .resolves(httpOkResponse(TOTP_MFA_SETUP_DTO)),

      setupSendSmsCode: sandbox
        .stub(client.auth.mfa.setup.sms, 'sendCode')
        .resolves(httpOkResponse({ validitySeconds: 60 })),
    };
  },
});
