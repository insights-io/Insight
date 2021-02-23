import { PwdChallengeResponseDTO } from '@rebrowse/sdk';
import { sandbox } from '@rebrowse/testing';
import { screen, waitFor } from '@testing-library/react';
import { client } from 'sdk';
import {
  PWD_CHALLENGE_SESSION_ID,
  SIGNIN_ROUTE,
} from 'shared/constants/routes';
import { getPage } from '__tests__/utils';

export const setup = async (route: string = SIGNIN_ROUTE) => {
  const { render } = await getPage({ route });
  render();
  return getElements();
};

export const getElements = () => {
  return {
    heading: screen.getByRole('heading', { name: 'Sign in to Rebrowse' }),
    emailInput: screen.getByPlaceholderText('john.doe@gmail.com'),
    continueButton: screen.getByRole('button', { name: 'Continue' }),
    createFreeAcountLink: screen.getByRole('link', {
      name: 'Create a free account',
    }),
    joinAnExistingTeamLink: screen.getByRole('link', {
      name: 'Join an existing team',
    }),
    orDivider: screen.getByText('Or'),
    signInWithGoogle: screen.getByRole('link', {
      name: /Sign in with Google/,
    }),
    signInWithGithub: screen.getByRole('link', {
      name: 'Sign in with Github',
    }),
    signInWithMicrosoft: screen.getByRole('link', {
      name: 'Sign in with Microsoft',
    }),
  };
};

export const findElements = () => {
  return waitFor(() => getElements());
};

export const chooseAccountPwdChallengeStub = (challengeId: string) => {
  return sandbox.stub(client.accounts, 'chooseAccount').callsFake(() => {
    document.cookie = `${PWD_CHALLENGE_SESSION_ID}=${challengeId}`;
    return Promise.resolve({
      statusCode: 200,
      headers: new Headers(),
      data: { action: 'PWD_CHALLENGE' },
    });
  });
};

export const chooseAccountSsoRedirectStub = (location: string) => {
  return sandbox.stub(client.accounts, 'chooseAccount').resolves({
    statusCode: 200,
    headers: new Headers(),
    data: { action: 'SSO_REDIRECT', location },
  });
};

export const retrievePasswordChallengeStub = (
  data: PwdChallengeResponseDTO
) => {
  return sandbox
    .stub(client.accounts, 'retrievePwdChallenge')
    .resolves({ headers: new Headers(), statusCode: 200, data });
};
