import React from 'react';
import type { Meta } from '@storybook/react';
import { configureStory } from '@rebrowse/storybook';
import { client } from 'sdk';
import { SinonSandbox } from 'sinon';

import { SignInMfaChallengePage } from './SignInMfaChallengePage';

const sendSmsCodeMock = (sandbox: SinonSandbox) => {
  return sandbox.stub(client.accounts, 'sendSmsCode').resolves({
    data: { validitySeconds: 60 },
    headers: new Headers(),
    statusCode: 200,
  });
};

export default {
  title: 'signin/pages/SignInMfaChallengePage',
  component: SignInMfaChallengePage,
} as Meta;

export const Base = () => {
  return <SignInMfaChallengePage methods={['totp', 'sms']} />;
};
Base.story = configureStory({
  setupMocks: sendSmsCodeMock,
});

export const Totp = () => {
  return <SignInMfaChallengePage methods={['totp']} />;
};

export const Sms = () => {
  return <SignInMfaChallengePage methods={['sms']} />;
};
