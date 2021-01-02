import { getPage } from 'next-page-tester';
import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { EMAIL_PLACEHOLDER } from 'shared/constants/form-placeholders';
import { sandbox } from '@rebrowse/testing';
import { mockApiError } from '@rebrowse/storybook';
import { PASSWORD_FORGOT_PAGE } from 'shared/constants/routes';
import { renderPage } from '__tests__/utils';
import { mockPasswordForgotPage } from '__tests__/mocks';

describe('/password-forgot', () => {
  /* Data */
  const email = 'john.doe@gmail.com';
  const route = PASSWORD_FORGOT_PAGE;

  test('As a user I can start password reset flow', async () => {
    /* Mocks */
    const { passwordForgotStub } = mockPasswordForgotPage(sandbox);

    /* Sever */
    const { page } = await getPage({ route });

    /* Client */
    renderPage(page);

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
    const { passwordForgotStub } = mockPasswordForgotPage(sandbox);
    passwordForgotStub.rejects(
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
    renderPage(page);

    await userEvent.type(screen.getByPlaceholderText(EMAIL_PLACEHOLDER), email);

    userEvent.click(screen.getByText('Reset password'));

    await screen.findByText('Bad Request');
    await screen.findByText('Invalid Email');

    sandbox.assert.calledWithExactly(passwordForgotStub, email);
  });

  test('As a user I can navigate to /login if I remember my password', async () => {
    /* Render */
    const { page } = await getPage({ route });
    renderPage(page);

    /* Assertions */
    userEvent.click(await screen.findByText('Remember password?'));
    await screen.findByText('Sign in with Google');
  });
});
