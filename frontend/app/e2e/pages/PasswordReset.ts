import { getByText } from '@testing-library/testcafe';

import { PASSWORD_RESET_PAGE } from '../../src/shared/constants/routes';

import { LoginPage } from '.';
import { AbstractPage } from './AbstractPage';

class PasswordReset extends AbstractPage {
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

export default new PasswordReset(PASSWORD_RESET_PAGE);
