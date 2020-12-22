import { sandbox } from '@rebrowse/testing';
import { AuthApi } from 'api';
import { getPage } from 'next-page-tester';
import { jsonPromise } from '__tests__/utils';
import userEvent from '@testing-library/user-event';
import { screen, render } from '@testing-library/react';
import { mockIndexPage } from '__tests__/mocks';
import { mockApiError } from '@rebrowse/storybook';

describe('/password-reset', () => {
  /* Data */
  const token = '1234';
  const password = 'password123';
  const route = `/password-reset?token=${token}`;

  describe('With existing password reset request', () => {
    test('As a user I get logged in after resetting my passsword', async () => {
      /* Mocks */
      const resetExistsStub = sandbox
        .stub(AuthApi.password, 'resetExists')
        .resolves({ data: true });

      const passwordResetStub = sandbox
        .stub(AuthApi.password, 'reset')
        .returns(jsonPromise({ status: 200 }));

      mockIndexPage();

      /* Render */
      const { page } = await getPage({ route });
      render(page);

      /* Assertions */
      sandbox.assert.calledWithMatch(resetExistsStub, token, {
        baseURL: 'http://localhost:8080',
      });

      expect(
        await screen.findByText('Reset your password')
      ).toBeInTheDocument();

      await userEvent.type(screen.getByPlaceholderText('Password'), password);

      userEvent.click(screen.getByText('Reset password and sign in'));

      document.cookie = 'SessionId=123';
      await screen.findByText('Page Visits');
      sandbox.assert.calledWithExactly(passwordResetStub, token, password);
    });

    test('As a user I can see error message if reset request fails', async () => {
      /* Mocks */
      const resetExistsStub = sandbox
        .stub(AuthApi.password, 'resetExists')
        .resolves({ data: true });

      const passwordResetStub = sandbox.stub(AuthApi.password, 'reset').rejects(
        mockApiError({
          statusCode: 400,
          reason: 'Bad Request',
          message: 'Bad Request',
          errors: {
            password: 'Too Short',
          },
        })
      );

      /* Render */
      const { page } = await getPage({ route });
      render(page);

      /* Assertions */
      sandbox.assert.calledWithMatch(resetExistsStub, token, {
        baseURL: 'http://localhost:8080',
      });

      expect(
        await screen.findByText('Reset your password')
      ).toBeInTheDocument();

      await userEvent.type(screen.getByPlaceholderText('Password'), password);

      userEvent.click(screen.getByText('Reset password and sign in'));

      await screen.findByText('Bad Request');
      await screen.findByText('Too Short');

      sandbox.assert.calledWithExactly(passwordResetStub, token, password);
    });
  });

  describe('With non existing password reset request', () => {
    test('As a user', async () => {
      /* Mocks */
      const resetExistsStub = sandbox
        .stub(AuthApi.password, 'resetExists')
        .resolves({ data: false });

      /* Render */
      const { page } = await getPage({ route });
      render(page);

      /* Assertions */
      sandbox.assert.calledWithMatch(resetExistsStub, token, {
        baseURL: 'http://localhost:8080',
      });

      await screen.findByText(
        'It looks like this password reset request is invalid or has already been accepted.'
      );

      userEvent.click(screen.getByText('Log in or reset your password'));
      await screen.findByText('Sign in with Google');
    });
  });
});