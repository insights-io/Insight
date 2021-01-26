import { parse } from 'querystring';

import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { authApiBaseUrl } from 'sdk';
import { appBaseUrl } from 'shared/config';
import {
  INDEX_ROUTE,
  REDIRECT_QUERY,
  SIGNIN_ROUTE,
} from 'shared/constants/routes';
import { getPage } from '__tests__/utils';
import { getQueryParam } from 'shared/utils/query';

const setup = async (route: string) => {
  const url = new URL(route.startsWith('/') ? `https://domain${route}` : route);

  const { render } = await getPage({ route });
  render();

  expect(
    screen.getByRole('heading', { name: 'Sign in to Rebrowse' })
  ).toBeInTheDocument();

  // inputs
  const emailInput = screen.getByPlaceholderText('john.doe@gmail.com');

  // buttons
  const continueButton = screen.getByRole('button', { name: 'Continue' });

  // links
  const createFreeAcountLink = screen.getByRole('link', {
    name: 'Create a free account',
  });
  const joinAnExistingTeamLink = screen.getByRole('link', {
    name: 'Join an existing team',
  });

  expect(screen.getByText('Or')).toBeInTheDocument();

  const redirect = encodeURIComponent(
    getQueryParam(parse(url.search.replace('?', '')), REDIRECT_QUERY) ||
      appBaseUrl
  );

  const signInWithGoogle = screen.getByRole('link', {
    name: /Sign in with Google/,
  });
  expect(signInWithGoogle).toHaveAttribute(
    'href',
    `${authApiBaseUrl}/v1/sso/oauth2/google/signin?redirect=${redirect}`
  );
  const signInWithGithub = screen.getByRole('link', {
    name: 'Sign in with Github',
  });
  expect(signInWithGithub).toHaveAttribute(
    'href',
    `${authApiBaseUrl}/v1/sso/oauth2/github/signin?${REDIRECT_QUERY}=${redirect}`
  );
  const signInWithMicrosoft = screen.getByRole('link', {
    name: 'Sign in with Microsoft',
  });
  expect(signInWithMicrosoft).toHaveAttribute(
    'href',
    `${authApiBaseUrl}/v1/sso/oauth2/microsoft/signin?${REDIRECT_QUERY}=${redirect}`
  );

  return {
    emailInput,
    continueButton,
    createFreeAcountLink,
    joinAnExistingTeamLink,
    signInWithGoogle,
    signInWithGithub,
    signInWithMicrosoft,
  };
};

describe.each([
  [INDEX_ROUTE, undefined],
  [INDEX_ROUTE, 'https://app.rebrowse.dev'],
  [SIGNIN_ROUTE, undefined],
  [SIGNIN_ROUTE, 'https://app.rebrowse.dev'],
])('%s?redirect=%s', (pathname, redirect) => {
  const route = redirect ? `${pathname}?redirect=${redirect}` : pathname;

  test('As a user I can navigate between "Password forgot?" and login without using email', async () => {
    const { emailInput, continueButton } = await setup(route);

    const email = 'john.doe@gmail.com';
    userEvent.type(emailInput, email);
    userEvent.click(continueButton);
    userEvent.click(await screen.findByRole('link', { name: 'Forgot?' }));

    // password-forgot page
    await screen.findByRole('heading', { name: 'Forgot password?' });
    expect(
      screen.getByRole('heading', {
        name:
          "Enter your email below and we'll send you a link to reset your password.",
      })
    ).toBeInTheDocument();

    // input is prefilled
    expect(screen.getByDisplayValue(email)).toBeInTheDocument();

    userEvent.click(screen.getByRole('link', { name: 'Remember password?' }));

    // index page
    await screen.findByRole('heading', { name: 'Sign in to Rebrowse' });
    expect(screen.getByDisplayValue(email)).toBeInTheDocument();
  });

  describe('As a user I want the page to be accessible', () => {
    test('Navigation via tabbing', async () => {
      const {
        emailInput,
        continueButton,
        createFreeAcountLink,
        joinAnExistingTeamLink,
        signInWithGoogle,
        signInWithGithub,
        signInWithMicrosoft,
      } = await setup(route);

      expect(document.activeElement).toEqual(emailInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(continueButton);
      userEvent.tab();
      expect(document.activeElement).toEqual(createFreeAcountLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(joinAnExistingTeamLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithGoogle);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithGithub);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithMicrosoft);
      userEvent.tab();
      expect(document.activeElement).toEqual(document.body);
      userEvent.tab();
      expect(document.activeElement).toEqual(emailInput);

      userEvent.type(emailInput, 'john.doe@gmail.com');
      userEvent.click(continueButton);

      const passwordInput = await screen.findByPlaceholderText('Password');
      const passwordForgotLink = screen.getByRole('link', {
        name: 'Forgot?',
      });
      const showPasswordButton = screen.getByRole('button', {
        name: 'Show password text',
      });

      expect(document.activeElement).toEqual(passwordInput);

      userEvent.tab({ shift: true });
      expect(document.activeElement).toEqual(passwordForgotLink);
      userEvent.tab();
      userEvent.tab();
      expect(document.activeElement).toEqual(showPasswordButton);
    });
  });
});
