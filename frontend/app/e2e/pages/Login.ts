import { getByPlaceholderText, queryByText } from '@testing-library/testcafe';

import config from '../config';
import { LOGIN_PAGE } from '../../src/shared/constants/routes';
import {
  EMAIL_PLACEHOLDER,
  WORK_EMAIL_PLACEHOLDER,
} from '../../src/shared/constants/form-placeholders';

import { AbstractPage } from './AbstractPage';

export type LoginCredentials = { email: string; password: string };

class Login extends AbstractPage {
  /* Selectors */
  public readonly emailInput = getByPlaceholderText(EMAIL_PLACEHOLDER);
  public readonly workEmailInput = getByPlaceholderText(WORK_EMAIL_PLACEHOLDER);
  public readonly samlSsoDisabledErrorMessage = queryByText(
    'That email or domain isn’t registered for SSO.'
  );
  public readonly passwordInput = getByPlaceholderText('Password');
  public readonly signInButton = queryByText('Sign in');
  public readonly forgotPasswordButton = queryByText('Forgot?');
  public readonly createFreeAccount = queryByText('Create a free account');

  public readonly tabs = {
    email: queryByText('Email'),
    sso: queryByText('SSO'),
  };

  public readonly errorMessages = {
    invalidEmail: queryByText('Please enter a valid email address'),
    passwordTooShort: queryByText(
      'Password must be at least 8 characters long'
    ),
    invalidCredentials: queryByText('Invalid email or password'),
  };

  public loginActions = (
    t: TestController,
    { email, password }: LoginCredentials
  ) => {
    return t
      .typeText(this.emailInput, email)
      .typeText(this.passwordInput, password)
      .click(this.signInButton);
  };

  /* Utils */
  public login = (t: TestController, credentials: LoginCredentials) => {
    return this.loginActions(t, credentials)
      .expect(this.signInButton.exists)
      .notOk('Sign in button is not visible anymore');
  };

  public loginWithRebrowseUser = (t: TestController) => {
    return this.login(t, {
      email: config.rebrowseUserEmail,
      password: config.rebrowseUserPassword,
    });
  };
}

export default new Login(LOGIN_PAGE);
