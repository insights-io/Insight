/* eslint-disable no-await-in-loop */
import { queryByPlaceholderText, queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';
import { totp as _totp } from 'speakeasy';

import { VERIFICATION_PAGE } from '../../src/shared/constants/routes';
import { findPatternInDockerLogs } from '../utils';

import { AbstractPage } from './AbstractPage';

class Verification extends AbstractPage {
  /* Selectors */
  public readonly message = queryByText(
    'To protect your account, please complete the following verification.'
  );

  public readonly mfaEnforcedMessage = queryByText(
    'Your organization has enforced multi-factor authentication for all members.'
  );

  public readonly codeInput = Selector(
    'input[aria-label="Please enter your pin code"]'
  );
  public readonly invalidCodeError = queryByText('Invalid code');
  public readonly expiredCodeError = queryByText('Code expired');
  public readonly submitButton = queryByText('Submit');

  public readonly tabs = {
    totp: { title: queryByText('Authy') },
    sms: {
      title: queryByText('Text message'),
      sendCode: queryByText('Send Code'),
      sendCodeSuccessMessage: queryByText('Code sent'),
      phoneNumberInput: queryByPlaceholderText('51111222'),
    },
  };

  /* Utils */
  public totp = (secret: string) => {
    return _totp({ secret, encoding: 'base32' });
  };

  private mfaLogin = async (
    t: TestController,
    codeProvider: (attempt: number) => Promise<string>,
    maxAttempts = 10
  ) => {
    let isValid = false;
    let attempt = 0;
    while (!isValid) {
      const code = await codeProvider(attempt);
      await t.typeText(this.codeInput, code).click(this.submitButton);
      isValid = !(
        (await this.invalidCodeError.visible) ||
        (await this.expiredCodeError.visible)
      );
      attempt++;
      if (attempt > maxAttempts) {
        throw new Error(`MFA Login failed after ${maxAttempts} attempts`);
      }
    }

    return t
      .expect(this.codeInput.visible)
      .notOk('Code input should not be visible anymore');
  };

  public completeTotpChallenge = async (t: TestController, secret: string) => {
    return this.mfaLogin(t, () => this.totp(secret));
  };

  public completeSmsChallenge = async (t: TestController) => {
    const smsPattern = /.*\[Rebrowse\] Verification code: (.*).*$/;
    const maxAttempts = 2;
    return this.mfaLogin(
      t,
      async () => {
        await t
          .click(this.tabs.sms.sendCode.with({ timeout: 60000 })) // in case retry is needed we need to wait for 60s to re-send code
          .expect(this.tabs.sms.sendCodeSuccessMessage.visible)
          .ok('Sucessfuly sent message');

        return findPatternInDockerLogs(smsPattern);
      },
      maxAttempts
    );
  };
}

export default new Verification(VERIFICATION_PAGE);
