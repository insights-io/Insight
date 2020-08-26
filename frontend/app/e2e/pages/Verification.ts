import { queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';
import { totp as _totp } from 'speakeasy';

import config from '../config';
import { findPatternInDockerLogs } from '../utils';

class Verification {
  public readonly path = `${config.appBaseURL}/login/verification`;

  /* Selectors */
  public readonly message = queryByText(
    'To protect your account, please complete the following verification.'
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
    },
  };

  /* Utils */
  public totp = (secret: string) => {
    return _totp({ secret, encoding: 'base32' });
  };

  private tfaLogin = async (t: TestController, codeProvider: () => string) => {
    let isValid = false;
    while (!isValid) {
      // eslint-disable-next-line no-await-in-loop
      await t.typeText(this.codeInput, codeProvider()).click(this.submitButton);
      // eslint-disable-next-line no-await-in-loop
      isValid = !(await this.invalidCodeError.visible);
    }
    return t
      .expect(this.codeInput.visible)
      .notOk('Code input should not be visible anymore');
  };

  public completeTotpChallenge = async (t: TestController, secret: string) => {
    return this.tfaLogin(t, () => this.totp(secret));
  };

  public completeSmsChallenge = async (t: TestController) => {
    await t
      .click(this.tabs.sms.sendCode)
      .expect(queryByText('Sucess').visible)
      .ok('Sucessfuly sent message');

    const smsPattern = /.*\[Insight\] Verification code: (.*).*$/;
    const verificationCode = findPatternInDockerLogs(smsPattern);
    return this.tfaLogin(t, () => verificationCode);
  };
}

export default new Verification();
