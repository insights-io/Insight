import React from 'react';
import { configureStory } from '@insight/storybook';
import AuthApi from 'api/auth';
import { INSIGHT_ADMIN, TFA_SETUP_QR_IMAGE } from 'test/data';
import { SWRConfig } from 'swr';
import { Meta } from '@storybook/react';

import { TwoFactorAuthentication } from './TwoFactorAuthentication';

export default {
  title: 'settings/components/TwoFactorAuthentication',
  component: TwoFactorAuthentication,
} as Meta;

export const TfaEnabled = () => {
  return (
    <SWRConfig value={{ dedupingInterval: 0 }}>
      <TwoFactorAuthentication user={INSIGHT_ADMIN} />
    </SWRConfig>
  );
};
TfaEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox
        .stub(AuthApi.tfa, 'listSetups')
        .resolves([{ createdAt: new Date().toUTCString(), method: 'totp' }]),
      setupStart: sandbox.stub(AuthApi.tfa.totp, 'setupStart').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox
        .stub(AuthApi.tfa, 'setupComplete')
        .resolves({ createdAt: new Date().toISOString(), method: 'totp' }),
      setupSendSmsCode: sandbox
        .stub(AuthApi.tfa.sms, 'setupSendCode')
        .resolves({ validitySeconds: 60 }),
    };
  },
});
