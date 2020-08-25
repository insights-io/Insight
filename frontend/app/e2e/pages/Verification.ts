import { queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';
import { totp as _totp } from 'speakeasy';

import config from '../config';

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

  /* Utils */
  public totp = (secret: string) => {
    return _totp({ secret, encoding: 'base32' });
  };

  public totpLogin = async (t: TestController, secret: string) => {
    let isValid = false;
    while (!isValid) {
      // eslint-disable-next-line no-await-in-loop
      await t
        .typeText(this.codeInput, this.totp(secret))
        .click(this.submitButton);
      // eslint-disable-next-line no-await-in-loop
      isValid = !(await this.invalidCodeError.visible);
    }
    return t
      .expect(this.codeInput.visible)
      .notOk('Code input should not be visible anymore');
  };
}

export default new Verification();
