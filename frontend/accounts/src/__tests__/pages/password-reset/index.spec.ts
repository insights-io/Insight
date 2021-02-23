import { sandbox } from '@rebrowse/testing';
import userEvent from '@testing-library/user-event';
import { PASSWORD_RESET_ROUTE } from 'shared/constants/routes';
import { v4 as uuid } from 'uuid';
import { INCLUDE_CREDENTIALS } from 'sdk';
import { waitFor } from '@testing-library/react';
import { appBaseUrl } from 'shared/config';
import * as windowUtils from 'shared/utils/window';
import { MfaMethod } from '@rebrowse/types';

import * as SignInMfaChallengeTestSetup from '../signin/challenge/mfa/SignInMfaChallengeTestSetup';
import * as SignInPageSetup from '../signin/SignInPageSetup';

import * as PasswordResetPageTestSetup from './PasswordResetPageTestSetup';
import * as PasswordResetNotFoundPageTestSetup from './PasswordResetNotFoundPageTestSetup';

describe('/password-reset', () => {
  const token = uuid();
  const tokenRoute = `${PASSWORD_RESET_ROUTE}?token=${token}`;
  const password = 'password1234';

  describe('Password reset exists', () => {
    describe('As a user I want to reset my password', () => {
      test('SUCCESS', async () => {
        const locationAssignStub = sandbox.stub(windowUtils, 'locationAssign');
        const passwordResetStub = PasswordResetPageTestSetup.passwordResetSuccessFlow();
        const passwordResetExistsStub = PasswordResetPageTestSetup.passwordResetExistsStub();
        const {
          continueButton,
          passwordInput,
        } = await PasswordResetPageTestSetup.setup(tokenRoute);
        sandbox.assert.calledWithExactly(passwordResetExistsStub, token);
        userEvent.type(passwordInput, password);
        userEvent.click(continueButton);

        await waitFor(() => {
          sandbox.assert.calledWithExactly(locationAssignStub, appBaseUrl);
        });

        sandbox.assert.calledWithExactly(
          passwordResetStub,
          { token, password },
          INCLUDE_CREDENTIALS
        );
      });

      test('MFA CHALLENGE', async () => {
        const challengeId = uuid();
        const methods: MfaMethod[] = ['sms', 'totp'];
        const passwordResetExistsStub = PasswordResetPageTestSetup.passwordResetExistsStub();
        const passwordResetStub = PasswordResetPageTestSetup.passwordResetMfaFlow(
          { methods, challengeId }
        );
        const retrieveMfaChallengeStub = SignInMfaChallengeTestSetup.retrieveMfaChallengeStub(
          methods
        );

        const {
          continueButton,
          passwordInput,
        } = await PasswordResetPageTestSetup.setup(tokenRoute);

        sandbox.assert.calledWithExactly(passwordResetExistsStub, token);
        userEvent.type(passwordInput, password);
        userEvent.click(continueButton);

        await SignInMfaChallengeTestSetup.findElements();
        sandbox.assert.calledWithExactly(
          passwordResetStub,
          { token, password },
          INCLUDE_CREDENTIALS
        );
        sandbox.assert.calledWithExactly(retrieveMfaChallengeStub, challengeId);
      });
    });

    describe('As a user I want the page to be accessible', () => {
      test('Navigation via tabbing', async () => {
        const passwordResetExistsStub = PasswordResetPageTestSetup.passwordResetExistsStub();
        const {
          continueButton,
          passwordInput,
          showPasswordButton,
        } = await PasswordResetPageTestSetup.setup(tokenRoute);
        sandbox.assert.calledWithExactly(passwordResetExistsStub, token);
        expect(document.activeElement).toEqual(passwordInput);
        userEvent.tab();
        expect(document.activeElement).toEqual(showPasswordButton);
        userEvent.tab();
        expect(document.activeElement).toEqual(continueButton);
        userEvent.tab();
        expect(document.activeElement).toEqual(document.body);
        userEvent.tab();
        expect(document.activeElement).toEqual(passwordInput);
      });
    });
  });

  describe('Password reset not found', () => {
    test('As a user I would like to navigate back to sign in page when invalid token', async () => {
      const passwordResetExistsStub = PasswordResetNotFoundPageTestSetup.passwordResetNotFoundStub();
      const { backToSignIn } = await PasswordResetNotFoundPageTestSetup.setup(
        tokenRoute
      );
      sandbox.assert.calledWithExactly(passwordResetExistsStub, token);
      userEvent.click(backToSignIn);
      await SignInPageSetup.findElements();
    });

    test('As a user I would like to navigate back to sign in page when no token', async () => {
      const { backToSignIn } = await PasswordResetNotFoundPageTestSetup.setup();
      userEvent.click(backToSignIn);
      await SignInPageSetup.findElements();
    });

    describe('As a user I want the page to be accessible', () => {
      test('Navigation via tabbing', async () => {
        const {
          backToSignIn,
        } = await PasswordResetNotFoundPageTestSetup.setup();
        expect(document.activeElement).toEqual(document.body);
        userEvent.tab();
        expect(document.activeElement).toEqual(backToSignIn);
        userEvent.tab();
        expect(document.activeElement).toEqual(document.body);
      });
    });
  });
});
