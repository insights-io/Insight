import { sandbox } from '@rebrowse/testing';
import { screen, waitFor } from '@testing-library/react';
import { client } from 'sdk';
import { PASSWORD_RESET_ROUTE } from 'shared/constants/routes';
import { getPage } from '__tests__/utils';

export const MATCHERS = {
  heading: ['heading', { name: 'Password reset not found' }],
  subheading: [
    'heading',
    {
      name:
        'It looks like this password reset request is invalid or has already been accepted.',
    },
  ],
  backToSignIn: ['link', { name: 'Back to sign in' }],
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
    subheading: screen.getByRole(...MATCHERS.subheading),
    backToSignIn: screen.getByRole(...MATCHERS.backToSignIn),
  };
};

export const passwordResetNotFoundStub = () => {
  return sandbox
    .stub(client.password, 'resetExists')
    .resolves({ data: false, statusCode: 200, headers: new Headers() });
};
