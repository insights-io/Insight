import { v4 as uuid } from 'uuid';

import { getLocation, findLinkFromDockerLogs, getTitle } from '../../utils';
import {
  Sidebar,
  AccountSettingsSecurityPage,
  AccountSettingsDetailsPage,
  SignUpPage,
  LoginPage,
  VerificationPage,
  PasswordForgotPage,
  PasswordResetPage,
} from '../../pages';

fixture('/login/verification').page(VerificationPage.path);

test('As a user I want to be challenged by TOTP MFA on password reset', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?redirect=%2F`);
  const { password, email } = SignUpPage.generateRandomCredentials();

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .expect(getLocation())
    .eql(AccountSettingsDetailsPage.path)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.mfa.authyToggle.parent())
    .typeText(
      AccountSettingsSecurityPage.mfa.authenticatorSetupModal.codeInput,
      '111111'
    )
    .click(AccountSettingsSecurityPage.mfa.authenticatorSetupModal.submitButton)
    .expect(
      AccountSettingsSecurityPage.mfa.authenticatorSetupModal.invalidCodeError
        .visible
    )
    .ok('Should display invalid code error');

  const secret = await AccountSettingsSecurityPage.mfa.setupAuthenticatorMfa(t);
  await t
    .expect(AccountSettingsSecurityPage.mfa.authyToggle.checked)
    .eql(true, 'TOTP MFA enabled message')
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.signOut)
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
    .eql(`${VerificationPage.path}?redirect=%2F`)
    .expect(getTitle())
    .eql('Verification')
    .expect(VerificationPage.tabs.totp.title.visible)
    .ok('TOTP MFA tab visible')
    .expect(VerificationPage.tabs.sms.title.visible)
    .notOk('SMS MFA tab visible');

  await VerificationPage.completeTotpChallenge(t, secret);
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.mfa.authyToggle.parent())
    .click(AccountSettingsSecurityPage.mfa.disableModal.confirmButton)
    .expect(AccountSettingsSecurityPage.mfa.authenticatorDisabledToast.visible)
    .ok('Should display message that TOTP MFA is disabled');
});

test('As a user I want to be challenged by TOTP MFA on login', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?redirect=%2F`);
  const { email, password } = SignUpPage.generateRandomCredentials();

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.mfa.authyToggle.parent())
    .typeText(
      AccountSettingsSecurityPage.mfa.authenticatorSetupModal.codeInput,
      '111111'
    )
    .click(AccountSettingsSecurityPage.mfa.authenticatorSetupModal.submitButton)
    .expect(
      AccountSettingsSecurityPage.mfa.authenticatorSetupModal.invalidCodeError
        .visible
    )
    .ok('Should display invalid code error');

  const secret = await AccountSettingsSecurityPage.mfa.setupAuthenticatorMfa(t);
  await t
    .expect(AccountSettingsSecurityPage.mfa.authenticatorEnabledToast.visible)
    .ok('TOTP MFA enabled message')
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.signOut);

  await LoginPage.login(t, { email, password })
    .expect(VerificationPage.message.visible)
    .ok('should display help message')
    .expect(getLocation())
    .eql(`${VerificationPage.path}?redirect=%2F`)
    .expect(VerificationPage.tabs.totp.title.visible)
    .ok('TOTP MFA tab visible')
    .expect(VerificationPage.tabs.sms.title.visible)
    .notOk('SMS MFA tab visible');

  await VerificationPage.completeTotpChallenge(t, secret);
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.mfa.authyToggle.parent())
    .click(AccountSettingsSecurityPage.mfa.disableModal.confirmButton)
    .expect(AccountSettingsSecurityPage.mfa.authenticatorDisabledToast.visible)
    .ok('Should display message that TOTP MFA is disabled');
});

test('As a user I want to be challenged by SMS MFA on password reset', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?redirect=%2F`);
  const { password, email } = SignUpPage.generateRandomCredentials();

  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
    phoneNumber: '51222333',
  });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.mfa.textMessageToggle.parent())
    .typeText(
      AccountSettingsSecurityPage.mfa.authenticatorSetupModal.codeInput,
      '111111'
    )
    .click(AccountSettingsSecurityPage.mfa.authenticatorSetupModal.submitButton)
    .expect(
      AccountSettingsSecurityPage.mfa.authenticatorSetupModal.expiredCodeError
        .visible
    )
    .ok('Should display invalid code error');

  await AccountSettingsSecurityPage.mfa.setupTextMessageMfa(t);
  await t
    .expect(AccountSettingsSecurityPage.mfa.textMessageToggle.checked)
    .eql(true, 'SMS MFA enabled message')
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.signOut)
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
    .eql(`${VerificationPage.path}?redirect=%2F`)
    .expect(VerificationPage.tabs.sms.title.visible)
    .ok('SMS MFA tab visible')
    .expect(VerificationPage.tabs.totp.title.visible)
    .notOk('TOTP MFA tab not visible');

  await VerificationPage.completeSmsChallenge(t);
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.mfa.textMessageToggle.parent())
    .click(AccountSettingsSecurityPage.mfa.disableModal.confirmButton)
    .expect(
      AccountSettingsSecurityPage.mfa.textMessageDisabledToastMessage.visible
    )
    .ok('Should display message that SMS MFA is disabled');
});

test('As a user I want to be challenged by TOTP & SMS MFA on login', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?redirect=%2F`);
  const { password, email } = SignUpPage.generateRandomCredentials();

  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
    phoneNumber: '51222444',
  });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings);

  await AccountSettingsDetailsPage.verifyCurrentPhoneNumber(t);
  await t
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.mfa.textMessageToggle.parent());

  await AccountSettingsSecurityPage.mfa.setupTextMessageMfa(t);

  await t.click(AccountSettingsSecurityPage.mfa.authyToggle.parent());
  const secret = await AccountSettingsSecurityPage.mfa.setupAuthenticatorMfa(t);
  await Sidebar.signOut(t);

  await LoginPage.login(t, { email, password })
    .expect(VerificationPage.tabs.totp.title.visible)
    .ok('TOTP tab visible')
    .expect(VerificationPage.tabs.sms.title.visible)
    .ok('SMS tab visible');

  // SMS verification
  await t.click(VerificationPage.tabs.sms.title);
  await VerificationPage.completeSmsChallenge(t);

  // Clean up
  await Sidebar.signOut(t);
  await LoginPage.login(t, { email, password });

  // TOTP verification
  await t.click(VerificationPage.tabs.totp.title);
  await VerificationPage.completeTotpChallenge(t, secret);

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.mfa.authyToggle.parent())
    .click(AccountSettingsSecurityPage.mfa.disableModal.confirmButton)
    .expect(AccountSettingsSecurityPage.mfa.authenticatorDisabledToast.visible)
    .ok('TOTP MFA disabled')
    .click(AccountSettingsSecurityPage.mfa.textMessageToggle.parent())
    .click(AccountSettingsSecurityPage.mfa.disableModal.confirmButton)
    .expect(
      AccountSettingsSecurityPage.mfa.textMessageDisabledToastMessage.visible
    )
    .ok('SMS MFA disabled');
});
