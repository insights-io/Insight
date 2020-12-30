import { getPage } from 'next-page-tester';
import userEvent from '@testing-library/user-event';
import { screen, render } from '@testing-library/react';
import { EMAIL_PLACEHOLDER } from 'shared/constants/form-placeholders';
import { sandbox } from '@rebrowse/testing';
import { AuthApi } from 'api';
import { mockApiError } from '@rebrowse/storybook';
import { PASSWORD_FORGOT_PAGE } from 'shared/constants/routes';

describe('/password-forgot', () => {
  /* Data */
  const email = 'john.doe@gmail.com';
  const route = PASSWORD_FORGOT_PAGE;

  test('As a user I can start password reset flow', async () => {
    /* Mocks */
    const passwordForgotStub = sandbox
      .stub(AuthApi.password, 'forgot')
      .resolves();

    /* Sever */
    const { page } = await getPage({ route });

    /* Client */
    render(page);

    /* Assertions */
    await screen.findByText(
      "Enter your email below and we'll send you a link to reset your password."
    );

    await userEvent.type(screen.getByPlaceholderText(EMAIL_PLACEHOLDER), email);
    userEvent.click(screen.getByText('Reset password'));

    await screen.findByText(
      'If your email address is associated with an Rebrowse account, you will be receiving a password reset request shortly.'
    );
    sandbox.assert.calledWithExactly(passwordForgotStub, email);
  });

  test('As a user I see error if something went wrong', async () => {
    /* Mocks */
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

    /* Server */
    const { page } = await getPage({ route });

    /* Client */
    render(page);

    const email = 'john.doe@gmail.com';
    await userEvent.type(screen.getByPlaceholderText(EMAIL_PLACEHOLDER), email);

    userEvent.click(screen.getByText('Reset password'));

    await screen.findByText('Bad Request');
    await screen.findByText('Invalid Email');

    sandbox.assert.calledWithExactly(passwordForgotStub, email);
  });

  test('As a user I can navigate to /login if I remember my password', async () => {
    /* Render */
    const { page } = await getPage({ route });
    render(page);

    /* Assertions */
    userEvent.click(await screen.findByText('Remember password?'));
    await screen.findByText('Sign in with Google');
  });
});
