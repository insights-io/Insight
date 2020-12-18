import { mockApiError } from '@rebrowse/storybook';
import { sandbox } from '@rebrowse/testing';
import { AuthApi } from 'api';
import { REBROWSE_ADMIN_DTO } from 'test/data/user';
import { getPage } from 'next-page-tester';
import { VERIFICATION_PAGE } from 'shared/constants/routes';
import { render, textPromise } from 'test/utils';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { mockIndexPage } from 'test/mocks';
import { TOTP_MFA_SETUP_QR_IMAGE } from 'test/data/mfa';

describe('/login/verification', () => {
  describe('With MFA setup', () => {
    test('As a user I can verify MFA using TOTP', async () => {
      document.cookie = 'ChallengeId=123';
      const getChallengeStub = sandbox
        .stub(AuthApi.mfa.challenge, 'get')
        .resolves(['totp']);

      const completeChallengeStub = sandbox
        .stub(AuthApi.mfa.challenge, 'complete')
        .callsFake(() => {
          document.cookie = 'SessionId=123';
          return textPromise({ status: 200 });
        });

      const { page } = await getPage({ route: VERIFICATION_PAGE });
      const { container } = render(page);

      sandbox.assert.calledWithMatch(getChallengeStub, '123', {
        baseURL: 'http://localhost:8080',
      });

      await screen.findByText(
        'To protect your account, please complete the following verification.'
      );

      const codeInput = container.querySelector(
        'input[aria-label="Please enter your pin code"]'
      ) as HTMLInputElement;

      const code = '123456';
      userEvent.type(codeInput, code);

      mockIndexPage();
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
      document.cookie = 'ChallengeId=123';
      const getChallengeStub = sandbox
        .stub(AuthApi.mfa.challenge, 'get')
        .resolves([]);

      const retrieveUserByChallengeStub = sandbox
        .stub(AuthApi.mfa.challenge, 'retrieveUser')
        .resolves(REBROWSE_ADMIN_DTO);

      const startTotpMfaSetupStub = sandbox
        .stub(AuthApi.mfa.setup.totp, 'start')
        .resolves({ data: { qrImage: TOTP_MFA_SETUP_QR_IMAGE } });

      const completeTotpMfaSetupStub = sandbox
        .stub(AuthApi.mfa.setup, 'completeEnforced')
        .callsFake(() => {
          document.cookie = 'SessionId=123';
          return Promise.resolve({
            createdAt: new Date().toISOString(),
            method: 'totp',
          });
        });

      const { page } = await getPage({ route: VERIFICATION_PAGE });
      const { container } = render(page);

      sandbox.assert.calledWithMatch(getChallengeStub, '123', {
        baseURL: 'http://localhost:8080',
      });

      sandbox.assert.calledWithMatch(retrieveUserByChallengeStub, '123', {
        baseURL: 'http://localhost:8080',
      });

      sandbox.assert.calledWithExactly(startTotpMfaSetupStub);

      await screen.findByText(
        'Your organization has enforced multi-factor authentication for all members.'
      );

      const codeInput = container.querySelector(
        'input[aria-label="Please enter your pin code"]'
      ) as HTMLInputElement;

      const code = '123456';
      userEvent.type(codeInput, code);

      mockIndexPage();
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
    const { page } = await getPage({ route: VERIFICATION_PAGE });
    render(page);
    await screen.findByText('Sign in with Google');
  });

  test('As a user I get redirected to /login when no challenge found on backend', async () => {
    document.cookie = 'ChallengeId=123';
    const getChallengeStub = sandbox.stub(AuthApi.mfa.challenge, 'get').rejects(
      mockApiError({
        statusCode: 404,
        reason: 'Not Found',
        message: 'Not Found',
      })
    );

    const { page } = await getPage({ route: VERIFICATION_PAGE });
    render(page);
    await screen.findByText('Sign in with Google');

    sandbox.assert.calledWithMatch(getChallengeStub, '123', {
      baseURL: 'http://localhost:8080',
    });
  });
});
