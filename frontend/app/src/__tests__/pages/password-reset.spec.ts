import { sandbox } from '@rebrowse/testing';
import { getPage } from 'next-page-tester';
import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { mockPasswordResetPage } from '__tests__/mocks';
import { mockApiError } from '@rebrowse/storybook';
import { renderPage } from '__tests__/utils';
import { match } from 'sinon';
import { INCLUDE_CREDENTIALS } from 'sdk';

describe('/password-reset', () => {
  /* Data */
  const token = '1234';
  const password = 'password123';
  const route = `/password-reset?token=${token}`;

  describe('With existing password reset request', () => {
    test('As a user I get logged in after resetting my passsword', async () => {
      /* Mocks */
      const { resetExistsStub, passwordResetStub } = mockPasswordResetPage(
        sandbox
      );

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithExactly(resetExistsStub, token, {
        headers: { 'uber-trace-id': (match.string as unknown) as string },
      });

      /* Client */
      renderPage(page);

      expect(
        await screen.findByText('Reset your password')
      ).toBeInTheDocument();

      await userEvent.type(screen.getByPlaceholderText('Password'), password);

      userEvent.click(screen.getByText('Reset password and sign in'));

      document.cookie = 'SessionId=123';
      await screen.findByText('Page Visits');
      sandbox.assert.calledWithExactly(
        passwordResetStub,
        token,
        password,
        INCLUDE_CREDENTIALS
      );
    });

    test('As a user I can see error message if reset request fails', async () => {
      /* Mocks */
      const { resetExistsStub, passwordResetStub } = mockPasswordResetPage(
        sandbox
      );

      passwordResetStub.rejects(
        mockApiError({
          statusCode: 400,
          reason: 'Bad Request',
          message: 'Bad Request',
          errors: { password: 'Too Short' },
        })
      );

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithExactly(resetExistsStub, token, {
        headers: { 'uber-trace-id': (match.string as unknown) as string },
      });

      /* Client */
      renderPage(page);

      expect(
        await screen.findByText('Reset your password')
      ).toBeInTheDocument();

      await userEvent.type(screen.getByPlaceholderText('Password'), password);

      userEvent.click(screen.getByText('Reset password and sign in'));

      await screen.findByText('Bad Request');
      await screen.findByText('Too Short');

      sandbox.assert.calledWithExactly(
        passwordResetStub,
        token,
        password,
        INCLUDE_CREDENTIALS
      );
    });
  });

  describe('With non existing password reset request', () => {
    test('As a user', async () => {
      /* Mocks */
      const { resetExistsStub } = mockPasswordResetPage(sandbox, {
        exists: false,
      });

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithExactly(resetExistsStub, token, {
        headers: { 'uber-trace-id': (match.string as unknown) as string },
      });

      /* Client */
      renderPage(page);

      await screen.findByText(
        'It looks like this password reset request is invalid or has already been accepted.'
      );

      userEvent.click(screen.getByText('Log in or reset your password'));
      await screen.findByText('Sign in with Google');
    });
  });
});
