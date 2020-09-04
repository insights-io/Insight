import { getByText } from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import config from '../config';
import { findLinkFromDockerLogs } from '../utils';
import {
  LoginPage,
  PasswordForgotPage,
  PasswordResetPage,
  Sidebar,
  SignUpPage,
} from '../pages';

fixture('/password-forgot').page(config.appBaseURL);

test('User should be able to reset password', async (t) => {
  const { email, password } = SignUpPage.generateRandomCredentials();

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .hover(Sidebar.accountSettings.item)
    .expect(Sidebar.accountSettings.accountSettings.visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.signOut)
    .click(LoginPage.forgotPasswordButton)
    .typeText(PasswordForgotPage.emailInput, email)
    .click(PasswordForgotPage.submitButton)
    .expect(PasswordForgotPage.requestSubmittedMessage.visible)
    .ok('Should display nice message that email has been sent');

  const passwordResetLink = findLinkFromDockerLogs();
  const newPassword = uuid();
  await t
    .navigateTo(passwordResetLink)
    .expect(PasswordResetPage.passwordInput.visible)
    .ok('Password input is visible')
    .typeText(PasswordResetPage.passwordInput, newPassword)
    .click(PasswordResetPage.submitButton)
    .expect(Sidebar.accountSettings.item.visible)
    .ok('Should be signed in to app');
});

test('User should be able to enter any email into apssword reset form and see message aboyut email being sent', async (t) => {
  await t
    .navigateTo('/password-forgot')
    .typeText(PasswordForgotPage.emailInput, 'short')
    .click(PasswordForgotPage.submitButton)
    .expect(getByText('Please enter a valid email address').visible)
    .ok('Should validate email input')
    .selectText(PasswordForgotPage.emailInput)
    .pressKey('delete')
    .typeText(PasswordForgotPage.emailInput, `not-so-short+${uuid()}@gmail.com`)
    .click(PasswordForgotPage.submitButton)
    .expect(PasswordForgotPage.requestSubmittedMessage.visible)
    .ok('Should display nice message that email has been sent');
});