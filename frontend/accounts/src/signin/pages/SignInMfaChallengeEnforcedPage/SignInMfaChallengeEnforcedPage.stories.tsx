import React from 'react';
import type { Meta } from '@storybook/react';
import {
  REBROWSE_ADMIN_NO_PHONE_NUMBER,
  REBROWSE_ADMIN,
  TOTP_SETUP_QR_IMAGE,
} from '@rebrowse/testing/src/fixtures';
import { configureStory } from '@rebrowse/storybook';
import { client } from 'sdk';
import { SinonSandbox } from 'sinon';

import { SignInMfaChallengeEnforcedPage } from './SignInMfaChallengeEnforcedPage';

export default {
  title: 'signin/pages/SignInMfaChallengeEnforcedPage',
  component: SignInMfaChallengeEnforcedPage,
} as Meta;

const totpSetupStartMock = (sandbox: SinonSandbox) => {
  return sandbox.stub(client.mfa.setup.totp, 'start').resolves({
    statusCode: 200,
    headers: new Headers(),
    data: {
      qrImage: TOTP_SETUP_QR_IMAGE,
    },
  });
};

export const Base = () => {
  return <SignInMfaChallengeEnforcedPage user={REBROWSE_ADMIN} />;
};
Base.story = configureStory({
  setupMocks: totpSetupStartMock,
});

export const NoPhoneNumber = () => {
  return (
    <SignInMfaChallengeEnforcedPage user={REBROWSE_ADMIN_NO_PHONE_NUMBER} />
  );
};
NoPhoneNumber.story = configureStory({
  setupMocks: totpSetupStartMock,
});
