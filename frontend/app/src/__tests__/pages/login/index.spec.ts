import { sandbox } from '@rebrowse/testing';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {
  EMAIL_PLACEHOLDER,
  WORK_EMAIL_PLACEHOLDER,
} from 'shared/constants/form-placeholders';
import { getPage } from 'next-page-tester';
import { LOGIN_PAGE } from 'shared/constants/routes';
import * as windowUtils from 'shared/utils/window';
import { mockLoginPage } from '__tests__/mocks';
import { match } from 'sinon';
import { renderPage } from '__tests__/utils';
import { client } from 'sdk';

describe('/login', () => {
  /* Data */
  const route = LOGIN_PAGE;

  describe('Email', () => {
    /* Data */
    const email = 'john.doe@gmail.com';
    const password = 'password1234!';

    test('As a user I can login using my email', async () => {
      /* Mocks */
      const { loginStub } = mockLoginPage(sandbox);

      /* Server */
      const { page } = await getPage({ route });

      /* Client */
      renderPage(page);

      const signInButton = await screen.findByText('Sign in');

      userEvent.type(screen.getByPlaceholderText(EMAIL_PLACEHOLDER), email);
      userEvent.type(screen.getByPlaceholderText('Password'), password);

      userEvent.click(signInButton);
      await screen.findByText('Page Visits');
      sandbox.assert.calledWithExactly(loginStub, email, password);
    });

    test('As a user I can login but get challenged by MFA', async () => {
      /* Mocks */
      const { retrieveChallengeStub, loginStub } = mockLoginPage(sandbox, {
        login: false,
      });

      /* Server */
      const { page } = await getPage({ route });

      /* Client */
      renderPage(page);

      const signInButton = await screen.findByText('Sign in');

      userEvent.type(screen.getByPlaceholderText(EMAIL_PLACEHOLDER), email);
      userEvent.type(screen.getByPlaceholderText('Password'), password);

      userEvent.click(signInButton);
      await screen.findByText(
        'To protect your account, please complete the following verification.'
      );

      sandbox.assert.calledWithExactly(retrieveChallengeStub, '123', {
        headers: { 'uber-trace-id': (match.string as unknown) as string },
      });

      sandbox.assert.calledWithExactly(loginStub, email, password);
    });
  });

  describe('SSO login', () => {
    test('As a user I see an error message if domain is not registered for SSO', async () => {
      /* Mocks */
      const { retrieveSsoSetupByDomainStub } = mockLoginPage(sandbox);

      /* Server */
      const { page } = await getPage({ route });

      /* Client */
      renderPage(page);

      userEvent.click(await screen.findByText('SSO'));
      userEvent.type(
        screen.getByPlaceholderText(WORK_EMAIL_PLACEHOLDER),
        'company@example.com'
      );

      userEvent.click(screen.getByText('Sign in'));
      await screen.findByText('That email or domain isn’t registered for SSO.');
      sandbox.assert.calledWithExactly(
        retrieveSsoSetupByDomainStub,
        'example.com'
      );
    });

    test('As a user I can login using my organization SSO provider', async () => {
      /* Mocks */
      const locationAsignStub = sandbox.stub(windowUtils, 'locationAssign');

      const ssoLocation = 'http://localhost:8080/sso/login';

      const { retrieveSsoSetupByDomainStub } = mockLoginPage(sandbox, {
        ssoSetupByDomain: ssoLocation,
      });

      /* Server */
      const { page } = await getPage({ route });

      /* Client */
      renderPage(page);

      userEvent.click(await screen.findByText('SSO'));
      userEvent.type(
        screen.getByPlaceholderText(WORK_EMAIL_PLACEHOLDER),
        'user@company.com'
      );

      userEvent.click(screen.getByText('Sign in'));
      await waitFor(() => {
        sandbox.assert.calledWithExactly(
          locationAsignStub,
          `${ssoLocation}?redirect=http%3A%2F%2Flocalhost%2F&email=user%40company.com`
        );
      });

      sandbox.assert.calledWithExactly(
        retrieveSsoSetupByDomainStub,
        'company.com'
      );
    });
  });

  describe('Social provider login', () => {
    test('As a user I should be able to start OAuth flow with providers', async () => {
      /* Server */
      const { page } = await getPage({ route });

      /* Client */
      renderPage(page);

      expect(
        screen.getByText('Sign in with Google').parentElement
      ).toHaveAttribute(
        'href',
        'http://localhost:8080/v1/sso/oauth2/google/signin?redirect=http%3A%2F%2Flocalhost%2F'
      );

      expect(
        screen.getByText('Sign in with Github').parentElement
      ).toHaveAttribute(
        'href',
        'http://localhost:8080/v1/sso/oauth2/github/signin?redirect=http%3A%2F%2Flocalhost%2F'
      );

      expect(
        screen.getByText('Sign in with Microsoft').parentElement
      ).toHaveAttribute(
        'href',
        'http://localhost:8080/v1/sso/oauth2/microsoft/signin?redirect=http%3A%2F%2Flocalhost%2F'
      );
    });
  });

  test('As a user I should be able to start password reset flow from login page', async () => {
    /* Mocks */
    const passwordForgotStub = sandbox
      .stub(client.auth.password, 'forgot')
      .resolves();

    /* Server */
    const { page } = await getPage({ route });

    /* Client */
    renderPage(page);

    userEvent.click(screen.getByText('Forgot?'));
    await screen.findByText('Remember password?');

    const email = 'user@gmail.com';
    await userEvent.type(
      screen.getByPlaceholderText('john.doe@gmail.com'),
      email
    );

    userEvent.click(screen.getByText('Reset password'));
    await screen.findByText('Check your inbox!');
    sandbox.assert.calledWithExactly(passwordForgotStub, email);
  });
});
