import { getAllByText } from '@testing-library/testcafe';

import { getLocation } from '../../utils';
import config from '../../config';
import { LoginPage, PasswordForgotPage } from '../../pages';

fixture('/login').page(config.appBaseURL);

test('Login email form should be validated both client & server side', async (t) => {
  await t
    .expect(getLocation())
    .eql(`${LoginPage.path}?redirect=%2F`)
    .click(LoginPage.signInButton)
    .expect(getAllByText('Required').count)
    .eql(2, 'Both fields should be required')
    .typeText(LoginPage.emailInput, 'random')
    .typeText(LoginPage.passwordInput, 'short')
    .click(LoginPage.signInButton)
    .expect(LoginPage.errorMessages.invalidEmail.visible)
    .ok('Should display email validation error message')
    .expect(LoginPage.errorMessages.passwordTooShort.visible)
    .ok('Should display password validation error message');

  await t
    .selectText(LoginPage.emailInput)
    .pressKey('delete')
    .typeText(LoginPage.emailInput, 'random@gmail.com')
    .expect(LoginPage.errorMessages.invalidEmail.exists)
    .notOk('Email validation error message should dissapear')
    .selectText(LoginPage.passwordInput)
    .pressKey('delete')
    .typeText(LoginPage.passwordInput, 'not_so_short')
    .expect(LoginPage.errorMessages.passwordTooShort.exists)
    .notOk('Password validation error message should dissapear')
    .click(LoginPage.signInButton)
    .expect(LoginPage.errorMessages.invalidCredentials.visible)
    .ok('Should display email/password miss-match message');
});

test('Login SAML SSO form should be validated', async (t) => {
  await t
    .expect(getLocation())
    .eql(`${LoginPage.path}?redirect=%2F`)
    .click(LoginPage.tabs.samlSso)
    .typeText(LoginPage.workEmailInput, 'matej.snuderl@insight.io')
    .click(LoginPage.signInButton)
    .expect(LoginPage.samlSsoDisabledErrorMessage.visible)
    .ok('Should display nice error message');
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
