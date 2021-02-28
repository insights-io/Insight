import React from 'react';
import type { Meta } from '@storybook/react';
import { configureStory } from '@rebrowse/storybook';
import { client } from 'sdk';
import { SinonSandbox } from 'sinon';
import { REBROWSE_ADMIN } from '@rebrowse/testing/src/fixtures';

import { SignInMfaChallengePage } from './SignInMfaChallengePage';

const setupMocks = (sandbox: SinonSandbox) => {
  return {
    sendSmsCode: sandbox.stub(client.accounts, 'sendSmsCode').resolves({
      data: { validitySeconds: 60 },
      headers: new Headers(),
      statusCode: 200,
    }),
  };
};

export default {
  title: 'signin/pages/SignInMfaChallengePage',
  component: SignInMfaChallengePage,
} as Meta;

export const Base = () => {
  return (
    <SignInMfaChallengePage methods={['totp', 'sms']} user={REBROWSE_ADMIN} />
  );
};
Base.story = configureStory({
  setupMocks,
});

export const Totp = () => {
  return <SignInMfaChallengePage methods={['totp']} user={REBROWSE_ADMIN} />;
};
Totp.story = configureStory({
  setupMocks,
});

export const Sms = () => {
  return <SignInMfaChallengePage methods={['sms']} user={REBROWSE_ADMIN} />;
};
Sms.story = configureStory({
  setupMocks,
});
