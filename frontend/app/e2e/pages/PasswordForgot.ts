import { getByText } from '@testing-library/testcafe';

import config from '../config';

import Login from './Login';

class PasswordForgot {
  public readonly path = `${config.appBaseURL}/password-forgot`;

  /* Selectors */
  public readonly emailInput = Login.emailInput;
  public readonly submitButton = getByText('Reset password');
  public readonly rememeberPassword = getByText('Remember password?');

  public readonly requestSubmittedMessage = getByText(
    'If your email address is associated with an Insight account, you will be receiving a password reset request shortly.'
  );
}

export default new PasswordForgot();
