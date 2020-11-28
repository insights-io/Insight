import React from 'react';
import { configureStory } from '@rebrowse/storybook';
import { AuthApi } from 'api/auth';
import { REBROWSE_ADMIN, TFA_SETUP_QR_IMAGE } from 'test/data';
import { SWRConfig } from 'swr';
import type { Meta } from '@storybook/react';

import { AccountMfaPanel } from './AccountMfaPanel';

export default {
  title: 'settings/components/account/AccountMfaPanel',
  component: AccountMfaPanel,
} as Meta;

export const TfaEnabled = () => {
  return (
    <SWRConfig value={{ dedupingInterval: 0 }}>
      <AccountMfaPanel user={REBROWSE_ADMIN} />
    </SWRConfig>
  );
};
TfaEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox
        .stub(AuthApi.tfa.setup, 'list')
        .resolves([{ createdAt: new Date().toUTCString(), method: 'totp' }]),
      setupStart: sandbox.stub(AuthApi.tfa.setup.totp, 'start').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox
        .stub(AuthApi.tfa.setup, 'complete')
        .resolves({ createdAt: new Date().toISOString(), method: 'totp' }),
      setupSendSmsCode: sandbox
        .stub(AuthApi.tfa.setup.sms, 'sendCode')
        .resolves({ validitySeconds: 60 }),
    };
  },
});
