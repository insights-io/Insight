import { getByText } from '@testing-library/testcafe';

import config from '../config';

import { LoginPage } from '.';

class PasswordReset {
  public readonly path = `${config.appBaseURL}/password-reset`;

  /* Selectors */
  public readonly passwordInput = LoginPage.passwordInput;
  public readonly submitButton = getByText('Reset password and sign in');

  public readonly passwordResetRequestNotFoundMessage = getByText(
    'It looks like this password reset request is invalid or has already been accepted.'
  );
  public readonly loginOrResetYourPasswordButton = getByText(
    'Log in or reset your password'
  );
}

export default new PasswordReset();
