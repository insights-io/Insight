import { getPage } from '__tests__/utils';
import { screen, waitFor } from '@testing-library/react';
import {
  MFA_CHALLENGE_SESSION_ID,
  SIGNIN_MFA_CHALLENGE_ROUTE,
} from 'shared/constants/routes';
import { v4 as uuid } from 'uuid';
import { sandbox } from '@rebrowse/testing';
import { client } from 'sdk';
import { MfaMethod } from '@rebrowse/types';

export const MATCHERS = {
  heading: ['heading', { name: 'Security verification' }],
  subheading: [
    'heading',
    {
      name:
        'To secure your account, please complete the following verification.',
    },
  ],
  googleAuthenticatorTab: ['tab', { name: 'Google Authenticator' }],
  smsTab: ['tab', { name: 'Text message' }],
  codeInput: ['Please enter your pin code'],
  continueButton: ['button', { name: 'Continue' }],
} as const;

export const getElements = () => {
  return {
    heading: screen.getByRole(...MATCHERS.heading),
    subheading: screen.getByRole(...MATCHERS.subheading),
    googleAuthenticatorTab: screen.getByRole(
      ...MATCHERS.googleAuthenticatorTab
    ),
    smsTab: screen.getByRole(...MATCHERS.smsTab),
    codeInput: screen.getAllByLabelText(...MATCHERS.codeInput),
    continueButton: screen.getByRole(...MATCHERS.continueButton),
  };
};

export const findElements = () => {
  return waitFor(() => getElements());
};

export const setup = async ({
  challengeId = uuid(),
  route = SIGNIN_MFA_CHALLENGE_ROUTE,
}: {
  challengeId?: string;
  route?: string;
}) => {
  document.cookie = `${MFA_CHALLENGE_SESSION_ID}=${challengeId}`;
  const { render } = await getPage({ route });
  render();
  const elements = await findElements();
  return { ...elements, challengeId };
};

export const retrieveMfaChallengeStub = (methods: MfaMethod[]) => {
  return sandbox.stub(client.accounts, 'retrieveMfaChallenge').resolves({
    statusCode: 200,
    headers: new Headers(),
    data: { methods },
  });
};
