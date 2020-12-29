import { mockApiError } from '@rebrowse/storybook';
import { sandbox } from '@rebrowse/testing';
import { AuthApi } from 'api';
import { REBROWSE_ADMIN_DTO } from '__tests__/data/user';
import { getPage } from 'next-page-tester';
import { VERIFICATION_PAGE } from 'shared/constants/routes';
import { screen, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { mockIndexPage } from '__tests__/mocks';
import { TOTP_MFA_SETUP_QR_IMAGE } from '__tests__/data/mfa';

describe('/login/verification', () => {
  /* Data */
  const route = VERIFICATION_PAGE;

  describe('With MFA setup', () => {
    test('As a user I can verify MFA using TOTP', async () => {
      /* Mocks */
      document.cookie = 'ChallengeId=123';
      mockIndexPage(sandbox);

      const getChallengeStub = sandbox
        .stub(AuthApi.mfa.challenge, 'get')
        .resolves({
          data: { data: ['totp'] },
          statusCode: 200,
          headers: new Headers(),
        });

      const completeChallengeStub = sandbox
        .stub(AuthApi.mfa.challenge, 'complete')
        .callsFake(() => {
          document.cookie = 'SessionId=123';
          return Promise.resolve({ statusCode: 200, headers: new Headers() });
        });

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithMatch(getChallengeStub, '123', {
        baseURL: 'http://localhost:8080',
      });

      /* Client */
      const { container } = render(page);

      await screen.findByText(
        'To protect your account, please complete the following verification.'
      );

      const codeInput = container.querySelector(
        'input[aria-label="Please enter your pin code"]'
      ) as HTMLInputElement;

      const code = '123456';
      userEvent.type(codeInput, code);

      userEvent.click(screen.getByText('Submit'));

      sandbox.assert.calledWithExactly(
        completeChallengeStub,
        'totp',
        Number(code)
      );

      // Client side navigation to / page
      await screen.findByText('Page Visits');
    });
  });

  describe('With no setup', () => {
    test('As a user I can setup TOTP MFA on the spot if no existing setup', async () => {
      /* Mocks */
      document.cookie = 'ChallengeId=123';
      const getChallengeStub = sandbox
        .stub(AuthApi.mfa.challenge, 'get')
        .resolves({
          data: { data: [] },
          statusCode: 200,
          headers: new Headers(),
        });

      const retrieveUserByChallengeStub = sandbox
        .stub(AuthApi.mfa.challenge, 'retrieveUser')
        .resolves({
          data: { data: REBROWSE_ADMIN_DTO },
          statusCode: 200,
          headers: new Headers(),
        });

      const startTotpMfaSetupStub = sandbox
        .stub(AuthApi.mfa.setup.totp, 'start')
        .resolves({
          data: { data: { qrImage: TOTP_MFA_SETUP_QR_IMAGE } },
          statusCode: 200,
          headers: new Headers(),
        });

      const completeTotpMfaSetupStub = sandbox
        .stub(AuthApi.mfa.setup, 'completeEnforced')
        .callsFake(() => {
          document.cookie = 'SessionId=123';
          return Promise.resolve({
            statusCode: 200,
            headers: new Headers(),
            data: {
              data: {
                createdAt: new Date().toISOString(),
                method: 'totp',
              },
            },
          });
        });

      mockIndexPage(sandbox);

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithMatch(getChallengeStub, '123', {
        baseURL: 'http://localhost:8080',
      });

      sandbox.assert.calledWithMatch(retrieveUserByChallengeStub, '123', {
        baseURL: 'http://localhost:8080',
      });

      /* Client */
      const { container } = render(page);

      await screen.findByText(
        'Your organization has enforced multi-factor authentication for all members.'
      );

      sandbox.assert.calledWithExactly(startTotpMfaSetupStub);

      const codeInput = container.querySelector(
        'input[aria-label="Please enter your pin code"]'
      ) as HTMLInputElement;

      const code = '123456';
      userEvent.type(codeInput, code);

      userEvent.click(screen.getByText('Submit'));

      // Client side navigation to / page
      await screen.findByText('Page Visits');

      sandbox.assert.calledWithExactly(
        completeTotpMfaSetupStub,
        'totp',
        123456
      );
    });
  });

  test('As a user I get redirected to /login when no challengeId cookie', async () => {
    /* Server */
    const { page } = await getPage({ route });

    /* Client */
    render(page);

    await screen.findByText('Sign in with Google');
  });

  test('As a user I get redirected to /login when no challenge found on backend', async () => {
    /* Mocks */
    document.cookie = 'ChallengeId=123';
    const getChallengeStub = sandbox.stub(AuthApi.mfa.challenge, 'get').rejects(
      mockApiError({
        statusCode: 404,
        reason: 'Not Found',
        message: 'Not Found',
      })
    );

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithMatch(getChallengeStub, '123', {
      baseURL: 'http://localhost:8080',
    });

    /* Client */
    render(page);

    await screen.findByText('Sign in with Google');
  });
});
