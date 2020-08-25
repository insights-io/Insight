import { v4 as uuid } from 'uuid';

import { getLocation, findLinkFromDockerLog } from '../../utils';
import {
  Sidebar,
  AccountSettingsPage,
  SignUpPage,
  LoginPage,
  VerificationPage,
  PasswordForgotPage,
  PasswordResetPage,
} from '../../pages';

fixture('/login/verification').page(VerificationPage.path);

test('Should be able to complete full TFA flow after password reset', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?dest=%2F`);
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, {
    fullName: 'Miha Novak',
    company: 'Insight',
    email,
    password,
  });

  await t
    .hover(Sidebar.accountSettings.item)
    .expect(Sidebar.accountSettings.accountSettings.visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .expect(getLocation())
    .eql(AccountSettingsPage.path)
    .click(AccountSettingsPage.twoFactorAuthentication)
    .typeText(AccountSettingsPage.tfa.codeInput, '111111')
    .click(AccountSettingsPage.tfa.submitButton)
    .expect(AccountSettingsPage.tfa.invalidCodeError.visible)
    .ok('Should display invalid code error');

  const tfaSecret = await AccountSettingsPage.tfa.extractQrCodeSecret();
  await t
    .typeText(
      AccountSettingsPage.tfa.codeInput,
      VerificationPage.totp(tfaSecret)
    )
    .expect(AccountSettingsPage.tfa.invalidCodeError.visible)
    .notOk('Should invalidate error on typing')
    .click(AccountSettingsPage.tfa.submitButton)
    .expect(AccountSettingsPage.tfa.enabledToast.visible)
    .ok('Should display positive message')
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

  const passwordResetLink = findLinkFromDockerLog();
  const newPassword = uuid();
  await t
    .navigateTo(passwordResetLink)
    .expect(PasswordResetPage.passwordInput.visible)
    .ok('Password input is visible')
    .typeText(PasswordResetPage.passwordInput, newPassword)
    .click(PasswordResetPage.submitButton)
    .expect(getLocation())
    .eql(`${VerificationPage.path}?dest=%2F`);

  await VerificationPage.totpLogin(t, tfaSecret);
  await t
    .hover(Sidebar.accountSettings.item)
    .expect(Sidebar.accountSettings.accountSettings.visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.twoFactorAuthentication)
    .expect(AccountSettingsPage.tfa.disabledToast.visible)
    .ok('Should display message that TFA is disabled');
});

test('Should be able to complete full TFA flow', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?dest=%2F`);
  const { email, password } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, {
    fullName: 'Miha Novak',
    company: 'Insight',
    email,
    password,
  });

  await t
    .hover(Sidebar.accountSettings.item)
    .expect(Sidebar.accountSettings.accountSettings.visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .expect(getLocation())
    .eql(AccountSettingsPage.path)
    .click(AccountSettingsPage.twoFactorAuthentication)
    .typeText(AccountSettingsPage.tfa.codeInput, '111111')
    .click(AccountSettingsPage.tfa.submitButton)
    .expect(AccountSettingsPage.tfa.invalidCodeError.visible)
    .ok('Should display invalid code error');

  const tfaSecret = await AccountSettingsPage.tfa.extractQrCodeSecret();
  await t
    .typeText(
      AccountSettingsPage.tfa.codeInput,
      VerificationPage.totp(tfaSecret)
    )
    .expect(AccountSettingsPage.tfa.invalidCodeError.visible)
    .notOk('Should invalidate error on typing')
    .click(AccountSettingsPage.tfa.submitButton)
    .expect(AccountSettingsPage.tfa.enabledToast.visible)
    .ok('Should display positive message')
    .hover(Sidebar.accountSettings.item)
    .expect(Sidebar.accountSettings.accountSettings.visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.signOut);

  await LoginPage.login(t, { email, password })
    .expect(VerificationPage.message.visible)
    .ok('should display help message')
    .expect(getLocation())
    .eql(`${VerificationPage.path}?dest=%2F`);

  await VerificationPage.totpLogin(t, tfaSecret);
  await t
    .hover(Sidebar.accountSettings.item)
    .expect(Sidebar.accountSettings.accountSettings.visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.twoFactorAuthentication)
    .expect(AccountSettingsPage.tfa.disabledToast.visible)
    .ok('Should display message that TFA is disabled');
});
