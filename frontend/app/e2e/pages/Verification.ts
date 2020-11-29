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

  private mfaLogin = async (t: TestController, codeProvider: () => string) => {
    let isValid = false;
    let tryCount = 0;
    while (!isValid) {
      // eslint-disable-next-line no-await-in-loop
      await t.typeText(this.codeInput, codeProvider()).click(this.submitButton);
      // eslint-disable-next-line no-await-in-loop
      isValid = !(await this.invalidCodeError.visible);
      tryCount++;
      if (tryCount > 10) {
        throw new Error('MFA Login failed after 10 attempts');
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
    await t
      .click(this.tabs.sms.sendCode)
      .expect(this.tabs.sms.sendCodeSuccessMessage.visible)
      .ok('Sucessfuly sent message');

    const smsPattern = /.*\[Rebrowse\] Verification code: (.*).*$/;
    const verificationCode = findPatternInDockerLogs(smsPattern);
    return this.mfaLogin(t, () => verificationCode);
  };
}

export default new Verification(VERIFICATION_PAGE);
