import { getPage } from '__tests__/utils';
import { screen, waitFor } from '@testing-library/react';
import {
  PWD_CHALLENGE_SESSION_ID,
  SESSION_ID,
  SIGNIN_PWD_CHALLENGE_ROUTE,
} from 'shared/constants/routes';
import { appBaseUrl } from 'shared/config';
import { v4 as uuid } from 'uuid';
import { sandbox } from '@rebrowse/testing';
import { client } from 'sdk';

import * as SignInPageSetup from '../../SignInPageSetup';

export const MATCHERS = {
  heading: ['heading', { name: "Verify it's you" }],
  accountSelector: (email: string) =>
    ['link', { name: new RegExp(email) }] as const,
  passwordInput: ['Password'],
  showPasswordButton: ['button', { name: 'Show password text' }],
  forgotPasswordLink: ['link', { name: 'Forgot?' }],
  continueButton: ['button', { name: 'Continue' }],
  createFreeAccountLink: ['link', { name: 'Create a free account' }],
  joinAnExistingTeamLink: ['link', { name: 'Join an existing team' }],
  signInWithGoogle: ['link', { name: /Sign in with Google/ }],
  signInWithGithub: ['link', { name: 'Sign in with Github' }],
  signInWithMicrosoft: ['link', { name: 'Sign in with Microsoft' }],
} as const;

export const getElements = (email: string) => {
  return {
    heading: screen.getByRole(...MATCHERS.heading),
    accountSelector: screen.getByRole(...MATCHERS.accountSelector(email)),
    passwordInput: screen.getByPlaceholderText(...MATCHERS.passwordInput),
    showPasswordButton: screen.getByRole(...MATCHERS.showPasswordButton),
    forgotPasswordLink: screen.getByRole(...MATCHERS.forgotPasswordLink),
    continueButton: screen.getByRole(...MATCHERS.continueButton),
    createFreeAccountLink: screen.getByRole(...MATCHERS.createFreeAccountLink),
    joinAnExistingTeamLink: screen.getByRole(
      ...MATCHERS.joinAnExistingTeamLink
    ),
    signInWithGoogle: screen.getByRole(...MATCHERS.signInWithGoogle),
    signInWithGithub: screen.getByRole(...MATCHERS.signInWithGithub),
    signInWithMicrosoft: screen.getByRole(...MATCHERS.signInWithMicrosoft),
  };
};

export const findElements = (email: string) => {
  return waitFor(() => getElements(email));
};

export const setup = async ({
  email,
  challengeId = uuid(),
  redirect = appBaseUrl,
  route = SIGNIN_PWD_CHALLENGE_ROUTE,
}: {
  email: string;
  challengeId?: string;
  redirect?: string;
  route?: string;
}) => {
  document.cookie = `${PWD_CHALLENGE_SESSION_ID}=${challengeId}`;
  const retrieveChallengeStub = SignInPageSetup.retrievePasswordChallengeStub({
    email,
    redirect,
  });
  const { render } = await getPage({ route });
  sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId);
  render();
  return { ...getElements(email), retrieveChallengeStub };
};

export const completePwdChellengeSuccess = ({
  location = appBaseUrl,
  sessionId = uuid(),
}: {
  sessionId?: string;
  location?: string;
} = {}) => {
  return sandbox.stub(client.accounts, 'completePwdChallenge').callsFake(() => {
    document.cookie = `${SESSION_ID}=${sessionId}`;
    return Promise.resolve({
      statusCode: 200,
      headers: new Headers(),
      data: { action: 'SUCCESS', location },
    });
  });
};
