import { v4 as uuid } from 'uuid';

import { getLocation, findLinkFromDockerLogs } from '../../utils';
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

test('[TOTP]: Should be able to complete full TFA flow after password reset', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?dest=%2F`);
  const { password, email } = SignUpPage.generateRandomCredentials();

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .hover(Sidebar.accountSettings.item)
    .expect(Sidebar.accountSettings.accountSettings.visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .expect(getLocation())
    .eql(AccountSettingsPage.path)
    .click(AccountSettingsPage.tfa.totp.checkbox)
    .typeText(AccountSettingsPage.tfa.codeInput, '111111')
    .click(AccountSettingsPage.tfa.submitButton)
    .expect(AccountSettingsPage.tfa.invalidCodeError.visible)
    .ok('Should display invalid code error');

  const tfaSecret = await AccountSettingsPage.tfa.totp.setup(t);
  await t
    .expect(AccountSettingsPage.tfa.totp.enabledToast.visible)
    .ok('TFA enabled message')
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
    .expect(getLocation())
    .eql(`${VerificationPage.path}?dest=%2F`)
    .expect(VerificationPage.tabs.totp.title.visible)
    .ok('TOTP TFA tab visible')
    .expect(VerificationPage.tabs.sms.title.visible)
    .notOk('SMS TFA tab visible');

  await VerificationPage.completeTotpChallenge(t, tfaSecret);
  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.tfa.totp.checkbox)
    .click(AccountSettingsPage.tfa.disableSubmitButton)
    .expect(AccountSettingsPage.tfa.totp.disabledToast.visible)
    .ok('Should display message that TOTP TFA is disabled');
});

test('[TOTP]: Should be able to complete full TFA flow', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?dest=%2F`);
  const { email, password } = SignUpPage.generateRandomCredentials();

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .hover(Sidebar.accountSettings.item)
    .expect(Sidebar.accountSettings.accountSettings.visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .expect(getLocation())
    .eql(AccountSettingsPage.path)
    .click(AccountSettingsPage.tfa.totp.checkbox)
    .typeText(AccountSettingsPage.tfa.codeInput, '111111')
    .click(AccountSettingsPage.tfa.submitButton)
    .expect(AccountSettingsPage.tfa.invalidCodeError.visible)
    .ok('Should display invalid code error');

  const tfaSecret = await AccountSettingsPage.tfa.totp.setup(t);
  await t
    .expect(AccountSettingsPage.tfa.totp.enabledToast.visible)
    .ok('TFA enabled message')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.signOut);

  await LoginPage.login(t, { email, password })
    .expect(VerificationPage.message.visible)
    .ok('should display help message')
    .expect(getLocation())
    .eql(`${VerificationPage.path}?dest=%2F`)
    .expect(VerificationPage.tabs.totp.title.visible)
    .ok('TOTP TFA tab visible')
    .expect(VerificationPage.tabs.sms.title.visible)
    .notOk('SMS TFA tab visible');

  await VerificationPage.completeTotpChallenge(t, tfaSecret);
  await t
    .hover(Sidebar.accountSettings.item)
    .expect(Sidebar.accountSettings.accountSettings.visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.tfa.totp.checkbox)
    .click(AccountSettingsPage.tfa.disableSubmitButton)
    .expect(AccountSettingsPage.tfa.totp.disabledToast.visible)
    .ok('Should display message that TOTP TFA is disabled');
});

test('[SMS]: Should be able to complete full TFA flow after password reset', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?dest=%2F`);
  const { password, email } = SignUpPage.generateRandomCredentials();

  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
    phoneNumber: '51222333',
  });

  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .expect(getLocation())
    .eql(AccountSettingsPage.path)
    .click(AccountSettingsPage.tfa.sms.checkbox)
    .typeText(AccountSettingsPage.tfa.codeInput, '111111')
    .click(AccountSettingsPage.tfa.submitButton)
    .expect(AccountSettingsPage.tfa.invalidCodeError.visible)
    .ok('Should display invalid code error');

  await AccountSettingsPage.tfa.sms.setup(t);
  await t
    .expect(AccountSettingsPage.tfa.sms.enabledToast.visible)
    .ok('TFA enabled message')
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
    .expect(getLocation())
    .eql(`${VerificationPage.path}?dest=%2F`)
    .expect(VerificationPage.tabs.sms.title.visible)
    .ok('SMS TFA tab visible')
    .expect(VerificationPage.tabs.totp.title.visible)
    .notOk('TOTP TFA tab not visible');

  await VerificationPage.completeSmsChallenge(t);
  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.tfa.sms.checkbox)
    .click(AccountSettingsPage.tfa.disableSubmitButton)
    .expect(AccountSettingsPage.tfa.sms.disabledToast.visible)
    .ok('Should display message that SMS TFA is disabled');
});
