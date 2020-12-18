import { getPage } from 'next-page-tester';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { EMAIL_PLACEHOLDER } from 'modules/auth/validation/email';
import { sandbox } from '@rebrowse/testing';
import { AuthApi } from 'api';
import { mockApiError } from '@rebrowse/storybook';

describe('/password-forgot', () => {
  test('As a user I can start password reset flow', async () => {
    const passwordForgotStub = sandbox
      .stub(AuthApi.password, 'forgot')
      .resolves();

    const { page } = await getPage({ route: '/password-forgot' });
    render(page);

    await screen.findByText(
      "Enter your email below and we'll send you a link to reset your password."
    );

    const email = 'john.doe@gmail.com';
    await userEvent.type(screen.getByPlaceholderText(EMAIL_PLACEHOLDER), email);

    userEvent.click(screen.getByText('Reset password'));

    await screen.findByText(
      'If your email address is associated with an Rebrowse account, you will be receiving a password reset request shortly.'
    );

    sandbox.assert.calledWithExactly(passwordForgotStub, email);
  });

  test('As a user I see error if something went wrong', async () => {
    const passwordForgotStub = sandbox.stub(AuthApi.password, 'forgot').rejects(
      mockApiError({
        message: 'Bad Request',
        reason: 'Bad Request',
        statusCode: 400,
        errors: {
          email: 'Invalid Email',
        },
      })
    );

    const { page } = await getPage({ route: '/password-forgot' });
    render(page);

    const email = 'john.doe@gmail.com';
    await userEvent.type(screen.getByPlaceholderText(EMAIL_PLACEHOLDER), email);

    userEvent.click(screen.getByText('Reset password'));

    await screen.findByText('Bad Request');
    await screen.findByText('Invalid Email');

    sandbox.assert.calledWithExactly(passwordForgotStub, email);
  });

  test('As a user I can navigate to /login if I remember my password', async () => {
    const { page } = await getPage({ route: '/password-forgot' });
    render(page);

    userEvent.click(await screen.findByText('Remember password?'));

    // Client side navigation to /login route
    await screen.findByText('Sign in with Google');
  });
});
