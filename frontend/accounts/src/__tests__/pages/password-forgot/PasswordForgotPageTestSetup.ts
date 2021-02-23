import { sandbox } from '@rebrowse/testing';
import { screen, waitFor } from '@testing-library/react';
import { client } from 'sdk';
import { PASSWORD_FORGOT_ROUTE } from 'shared/constants/routes';
import { getPage } from '__tests__/utils';

export const MATCHERS = {
  heading: ['heading', { name: 'Forgot password?' }],
  subheading: [
    'heading',
    {
      name:
        "Enter your email below and we'll send you a link to reset your password.",
    },
  ],
  rememberPasswordLink: ['link', { name: 'Remember password?' }],
  emailInputPlaceholder: ['john.doe@gmail.com'],
  continueButton: ['button', { name: 'Continue' }],
} as const;

export const setup = async (route = PASSWORD_FORGOT_ROUTE) => {
  const { render } = await getPage({ route });
  render();
  return getElements();
};

export const findElements = () => {
  return waitFor(() => getElements());
};

export const getElements = () => {
  return {
    heading: screen.getByRole(...MATCHERS.heading),
    subheading: screen.getByRole(...MATCHERS.subheading),
    rememberPasswordLink: screen.getByRole(...MATCHERS.rememberPasswordLink),
    emailInput: screen.getByPlaceholderText(...MATCHERS.emailInputPlaceholder),
    continueButton: screen.getByRole(...MATCHERS.continueButton),
  };
};

export const passwordForgotStub = () => {
  return sandbox
    .stub(client.password, 'forgot')
    .resolves({ statusCode: 200, headers: new Headers() });
};
