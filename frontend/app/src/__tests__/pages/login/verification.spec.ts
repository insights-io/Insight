import { mockApiError } from '@rebrowse/storybook';
import { sandbox } from '@rebrowse/testing';
import { getPage } from 'next-page-tester';
import { VERIFICATION_PAGE } from 'shared/constants/routes';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { mockVerificationPage } from '__tests__/mocks';
import { match } from 'sinon';
import { renderPage } from '__tests__/utils';
import { client } from 'sdk';

describe('/login/verification', () => {
  /* Data */
  const route = VERIFICATION_PAGE;
  const challengeId = '123';

  describe('With MFA setup', () => {
    test('As a user I can verify MFA using TOTP', async () => {
      /* Mocks */
      document.cookie = `ChallengeId=${challengeId}`;
      const {
        retrieveChallengeStub,
        completeChallengeStub,
      } = mockVerificationPage(sandbox);

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId, {
        headers: { 'uber-trace-id': (match.string as unknown) as string },
      });

      /* Client */
      const { container } = renderPage(page);

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
      document.cookie = `ChallengeId=${challengeId}`;
      const {
        retrieveChallengeStub,
        retrieveUserByChallengeStub,
        startTotpMfaSetupStub,
        completeTotpMfaSetupStub,
      } = mockVerificationPage(sandbox, { methods: [] });

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId, {
        headers: { 'uber-trace-id': (match.string as unknown) as string },
      });

      sandbox.assert.calledWithExactly(
        retrieveUserByChallengeStub,
        challengeId,
        { headers: { 'uber-trace-id': (match.string as unknown) as string } }
      );

      /* Client */
      const { container } = renderPage(page);

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
    renderPage(page);

    await screen.findByText('Sign in with Google');
  });

  test('As a user I get redirected to /login when no challenge found on backend', async () => {
    /* Mocks */
    document.cookie = `ChallengeId=${challengeId}`;
    const retrieveChallengeStub = sandbox
      .stub(client.auth.mfa.challenge, 'retrieve')
      .rejects(
        mockApiError({
          statusCode: 404,
          reason: 'Not Found',
          message: 'Not Found',
        })
      );

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId, {
      headers: { 'uber-trace-id': (match.string as unknown) as string },
    });

    /* Client */
    renderPage(page);

    await screen.findByText('Sign in with Google');
  });
});
