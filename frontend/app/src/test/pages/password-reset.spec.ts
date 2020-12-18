import { sandbox } from '@rebrowse/testing';
import { AuthApi } from 'api';
import { getPage } from 'next-page-tester';
import { render, jsonPromise } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { mockIndexPage } from 'test/mocks';
import { mockApiError } from '@rebrowse/storybook';

describe('/password-reset', () => {
  describe('With existing password reset request', () => {
    test('As a user I get logged in after resetting my passsword', async () => {
      const resetExistsStub = sandbox
        .stub(AuthApi.password, 'resetExists')
        .resolves({ data: true });

      const passwordResetStub = sandbox
        .stub(AuthApi.password, 'reset')
        .returns(jsonPromise({ status: 200 }));

      const { page } = await getPage({ route: '/password-reset?token=1234' });
      render(page);

      sandbox.assert.calledWithMatch(resetExistsStub, '1234', {
        baseURL: 'http://localhost:8080',
      });

      expect(
        await screen.findByText('Reset your password')
      ).toBeInTheDocument();

      await userEvent.type(
        screen.getByPlaceholderText('Password'),
        'password123'
      );

      mockIndexPage();

      userEvent.click(screen.getByText('Reset password and sign in'));

      // Client side navigation to / route
      await screen.findByText('Page Visits');

      sandbox.assert.calledWithExactly(
        passwordResetStub,
        '1234',
        'password123'
      );
    });

    test('As a user I can see error message if reset request fails', async () => {
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

      const { page } = await getPage({ route: '/password-reset?token=1234' });

      sandbox.assert.calledWithMatch(resetExistsStub, '1234', {
        baseURL: 'http://localhost:8080',
      });

      render(page);

      expect(
        await screen.findByText('Reset your password')
      ).toBeInTheDocument();

      await userEvent.type(
        screen.getByPlaceholderText('Password'),
        'password123'
      );

      userEvent.click(screen.getByText('Reset password and sign in'));

      await screen.findByText('Bad Request');
      await screen.findByText('Too Short');

      sandbox.assert.calledWithExactly(
        passwordResetStub,
        '1234',
        'password123'
      );
    });
  });

  describe('With non existing password reset request', () => {
    test('As a user', async () => {
      const resetExistsStub = sandbox
        .stub(AuthApi.password, 'resetExists')
        .resolves({ data: false });

      const { page } = await getPage({ route: '/password-reset?token=1234' });

      sandbox.assert.calledWithMatch(resetExistsStub, '1234', {
        baseURL: 'http://localhost:8080',
      });

      render(page);

      await screen.findByText(
        'It looks like this password reset request is invalid or has already been accepted.'
      );

      userEvent.click(screen.getByText('Log in or reset your password'));

      // Client side navigation to /login route
      await screen.findByText('Sign in with Google');
    });
  });
});
