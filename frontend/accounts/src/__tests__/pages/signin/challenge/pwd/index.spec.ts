import userEvent from '@testing-library/user-event';
import { waitFor } from '@testing-library/react';
import { sandbox } from '@rebrowse/testing';
import * as windowUtils from 'shared/utils/window';
import { appBaseUrl } from 'shared/config';
import { INCLUDE_CREDENTIALS } from 'sdk';

import * as PasswordForgotPageTestSetup from '../../../password-forgot/PasswordForgotPageTestSetup';
import * as SignInTestSetup from '../../SignInPageSetup';
import signInMfaChallengeTestPage from '../mfa/SignInMfaChallengeTestPage';

import signInPwdChallengeTestSetup, {
  SignInPwdChallangeTestPage,
} from './SignInPwdChallengeTestPage';

describe('/signin/challenge/pwd', () => {
  const email = 'john.doe@gmail.com';
  const password = 'password1234!';

  test('As a user I can succesffuly complete pwd challenge & get challanged with MFA', async () => {
    const {
      challengeId,
      completePwdChallengeStub,
      retrieveMfaChallengeStub,
    } = SignInPwdChallangeTestPage.completePwdChallengeMfa();

    const {
      passwordInput,
      continueButton,
    } = await signInPwdChallengeTestSetup.setup({ email });

    userEvent.type(passwordInput, password);
    userEvent.click(continueButton);
    await signInMfaChallengeTestPage.findElements();

    sandbox.assert.calledWithExactly(
      completePwdChallengeStub,
      { email, password },
      INCLUDE_CREDENTIALS
    );

    sandbox.assert.calledWithExactly(retrieveMfaChallengeStub, challengeId);
  });

  test('As a user I can successfully complete pwd challenge & get redirected back to the app', async () => {
    const locationAssignStub = sandbox.stub(windowUtils, 'locationAssign');
    const completePwdChallengeStub = SignInPwdChallangeTestPage.completePwdChallengeSuccess();
    const {
      passwordInput,
      continueButton,
    } = await signInPwdChallengeTestSetup.setup({ email });
    userEvent.type(passwordInput, password);
    userEvent.click(continueButton);
    await waitFor(() => {
      sandbox.assert.calledWithExactly(locationAssignStub, appBaseUrl);
    });
    sandbox.assert.calledWithExactly(
      completePwdChallengeStub,
      { email, password },
      INCLUDE_CREDENTIALS
    );
  });

  test('As a user I can sign in with different account by navigating back to /signin page', async () => {
    const { accountSelector } = await signInPwdChallengeTestSetup.setup({
      email,
    });
    userEvent.click(accountSelector);
    await SignInTestSetup.findElements();
  });

  test('As a user I should be able to navigate back and forth to password forgot page', async () => {
    const { forgotPasswordLink } = await signInPwdChallengeTestSetup.setup({
      email,
    });
    userEvent.click(forgotPasswordLink);
    const {
      emailInput,
      rememberPasswordLink,
    } = await PasswordForgotPageTestSetup.findElements();
    expect(emailInput).toHaveValue(email);
    userEvent.click(rememberPasswordLink);
    await signInPwdChallengeTestSetup.findElements(email);
  });

  describe('As a user I want the page to be accessible', () => {
    test('Navigation via tabbing', async () => {
      const {
        passwordInput,
        showPasswordButton,
        continueButton,
        createFreeAccountLink,
        joinAnExistingTeamLink,
        signInWithGoogle,
        signInWithGithub,
        signInWithMicrosoft,
        accountSelector,
        forgotPasswordLink,
      } = await signInPwdChallengeTestSetup.setup({ email });

      expect(document.activeElement).toEqual(passwordInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(showPasswordButton);
      userEvent.tab();
      expect(document.activeElement).toEqual(continueButton);
      userEvent.tab();
      expect(document.activeElement).toEqual(createFreeAccountLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(joinAnExistingTeamLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithGoogle);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithGithub);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithMicrosoft);
      userEvent.tab();
      expect(document.activeElement).toEqual(document.body);
      userEvent.tab();
      expect(document.activeElement).toEqual(accountSelector);
      userEvent.tab();
      expect(document.activeElement).toEqual(forgotPasswordLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(passwordInput);
    });
  });
});
