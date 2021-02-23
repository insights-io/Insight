import { sandbox } from '@rebrowse/testing';
import { screen, waitFor } from '@testing-library/react';
import { client } from 'sdk';
import { appBaseUrl } from 'shared/config';
import {
  MFA_CHALLENGE_SESSION_ID,
  PASSWORD_RESET_ROUTE,
  SESSION_ID,
} from 'shared/constants/routes';
import { getPage } from '__tests__/utils';
import { v4 as uuid } from 'uuid';
import { MfaMethod } from '@rebrowse/types';

export const MATCHERS = {
  heading: ['heading', { name: 'Reset your password' }],
  passwordInput: ['Password'],
  showPasswordButton: ['button', { name: 'Show password text' }],
  continueButton: ['button', { name: 'Continue' }],
} as const;

export const setup = async (route = PASSWORD_RESET_ROUTE) => {
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
    passwordInput: screen.getByPlaceholderText(...MATCHERS.passwordInput),
    showPasswordButton: screen.getByRole(...MATCHERS.showPasswordButton),
    continueButton: screen.getByRole(...MATCHERS.continueButton),
  };
};

export const passwordResetExistsStub = () => {
  return sandbox
    .stub(client.password, 'resetExists')
    .resolves({ data: true, statusCode: 200, headers: new Headers() });
};

export const passwordResetSuccessFlow = ({
  location = appBaseUrl,
  sessionId = uuid(),
}: {
  sessionId?: string;
  location?: string;
} = {}) => {
  return sandbox.stub(client.password, 'reset').callsFake(() => {
    document.cookie = `${SESSION_ID}=${sessionId}`;
    return Promise.resolve({
      statusCode: 200,
      headers: new Headers(),
      data: { action: 'SUCCESS', location },
    });
  });
};

export const passwordResetMfaFlow = ({
  challengeId = uuid(),
  methods = ['sms', 'totp'],
}: {
  challengeId?: string;
  methods?: MfaMethod[];
} = {}) => {
  return sandbox.stub(client.password, 'reset').callsFake(() => {
    document.cookie = `${MFA_CHALLENGE_SESSION_ID}=${challengeId}`;
    return Promise.resolve({
      statusCode: 200,
      headers: new Headers(),
      data: { action: 'MFA_CHALLENGE', challengeId, methods },
    });
  });
};
