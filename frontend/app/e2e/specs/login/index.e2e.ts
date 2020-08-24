import {
  getByText,
  queryByText,
  getAllByText,
} from '@testing-library/testcafe';

import { getLocation } from '../../utils';
import config from '../../config';
import { LoginPage, PasswordForgotPage } from '../../pages';

fixture('/login').page(config.appBaseURL);

test('Login form should be validated both client & server side', async (t) => {
  const invalidEmailErrorMessage = 'Please enter a valid email address';
  const passwordTooShortErrorMessage =
    'Password must be at least 8 characters long';

  await t
    .expect(getLocation())
    .eql(`${LoginPage.path}?dest=%2F`)
    .click(LoginPage.signInButton)
    .expect(getAllByText('Required').count)
    .eql(2, 'Both fields should be required')
    .typeText(LoginPage.emailInput, 'random')
    .typeText(LoginPage.passwordInput, 'short')
    .click(LoginPage.signInButton)
    .expect(getByText(invalidEmailErrorMessage).visible)
    .ok('Should display email validation error message')
    .expect(getByText(passwordTooShortErrorMessage).visible)
    .ok('Should display password validation error message');

  await t
    .selectText(LoginPage.emailInput)
    .pressKey('delete')
    .typeText(LoginPage.emailInput, 'random@gmail.com')
    .expect(queryByText(invalidEmailErrorMessage).exists)
    .notOk('Email validation error message should dissapear')
    .selectText(LoginPage.passwordInput)
    .pressKey('delete')
    .typeText(LoginPage.passwordInput, 'not_so_short')
    .expect(queryByText(passwordTooShortErrorMessage).exists)
    .notOk('Password validation error message should dissapear')
    .click(LoginPage.signInButton)
    .expect(getByText('Invalid email or password').visible)
    .ok('Should display email/password miss-match message');
});

test('Can navigate forth and back to /password/forgot', async (t) => {
  await t
    .click(LoginPage.forgotPasswordButton)
    .expect(getLocation())
    .eql(PasswordForgotPage.path)
    .click(PasswordForgotPage.rememeberPassword)
    .expect(getLocation())
    .eql(LoginPage.path);
});
