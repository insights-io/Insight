import { getByText } from '@testing-library/testcafe';

import { PASSWORD_FORGOT_PAGE } from '../../src/shared/constants/routes';

import { AbstractPage } from './AbstractPage';
import Login from './Login';

class PasswordForgot extends AbstractPage {
  /* Selectors */
  public readonly emailInput = Login.emailInput;
  public readonly submitButton = getByText('Reset password');
  public readonly rememeberPassword = getByText('Remember password?');

  public readonly requestSubmittedMessage = getByText(
    'If your email address is associated with an Rebrowse account, you will be receiving a password reset request shortly.'
  );
}

export default new PasswordForgot(PASSWORD_FORGOT_PAGE);
