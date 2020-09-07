import 'testcafe';
import { getByPlaceholderText, queryByText } from '@testing-library/testcafe';

import config from '../config';

export type LoginCredentials = { email: string; password: string };

class Login {
  public readonly path = `${config.appBaseURL}/login`;

  /* Selectors */
  public readonly emailInput = getByPlaceholderText('Email');
  public readonly workEmailInput = getByPlaceholderText('user@company.com');
  public readonly samlSsoDisabledErrorMessage = queryByText(
    'That email or domain isn’t registered for SSO.'
  );
  public readonly passwordInput = getByPlaceholderText('Password');
  public readonly signInButton = queryByText('Sign in');
  public readonly forgotPasswordButton = queryByText('Forgot?');
  public readonly createFreeAccount = queryByText('Create a free account');

  public readonly tabs = {
    email: queryByText('Email'),
    samlSso: queryByText('SAML SSO'),
  };

  public readonly errorMessages = {
    invalidEmail: queryByText('Please enter a valid email address'),
    passwordTooShort: queryByText(
      'Password must be at least 8 characters long'
    ),
    invalidCredentials: queryByText('Invalid email or password'),
  };

  /* Utils */
  public login = (t: TestController, { email, password }: LoginCredentials) => {
    return t
      .typeText(this.emailInput, email)
      .typeText(this.passwordInput, password)
      .click(this.signInButton)
      .expect(this.signInButton.exists)
      .notOk('Sign in button is not visible anymore');
  };

  public loginWithInsightUser = (t: TestController) => {
    return this.login(t, {
      email: config.insightUserEmail,
      password: config.insightUserPassword,
    });
  };
}

export default new Login();
