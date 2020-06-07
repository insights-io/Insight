import { getByPlaceholderText, getByText } from '@testing-library/testcafe';

import config from '../config';

import { findLinkFromDockerLog } from './mail';

/* Login */
export const emailInput = getByPlaceholderText('Email');
export const passwordInput = getByPlaceholderText('Password');
export const signInButton = getByText('Sign in');

/* Sign up */
export const fullNameInput = getByPlaceholderText('Full name');
export const companyInput = getByPlaceholderText('Company');
export const getStartedButton = getByText('Get started');

/* Password forgot */
export const forgotPasswordButton = getByText('Forgot?');
export const startPasswordResetButton = getByText('Reset password');
export const finishPasswordResetButton = getByText(
  'Reset password and sign in'
);

type LoginCredentials = { email: string; password: string };

export const login = (
  t: TestController,
  { email, password }: LoginCredentials
) => {
  return t
    .typeText(emailInput, email)
    .typeText(passwordInput, password)
    .click(signInButton);
};

type SignUpDetails = LoginCredentials & {
  fullName: string;
  company: string;
};

export const signUp = (
  t: TestController,
  { fullName, company, email, password }: SignUpDetails
) => {
  return t
    .typeText(fullNameInput, fullName)
    .typeText(companyInput, company)
    .typeText(emailInput, email)
    .typeText(passwordInput, password)
    .click(getStartedButton);
};

export const signUpVerifyEmail = (t: TestController) => {
  const link = findLinkFromDockerLog();
  if (!link) {
    throw new Error('Sign up link not found');
  }

  return t.navigateTo(link);
};

export const signUpAndLogin = async (
  t: TestController,
  data: SignUpDetails
) => {
  await t.navigateTo(config.tryBaseURL);
  await signUp(t, data);
  return signUpVerifyEmail(t);
};
