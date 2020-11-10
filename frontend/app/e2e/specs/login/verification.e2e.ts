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

test('[TFA](TOTP): User should be able to complete full TFA flow after password reset', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?redirect=%2F`);
  const { password, email } = SignUpPage.generateRandomCredentials();

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .expect(getLocation())
    .eql(AccountSettingsDetailsPage.path)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.tfa.authenticatorTfaCheckbox)
    .typeText(
      AccountSettingsSecurityPage.tfa.authenticatorSetupModal.codeInput,
      '111111'
    )
    .click(AccountSettingsSecurityPage.tfa.authenticatorSetupModal.submitButton)
    .expect(
      AccountSettingsSecurityPage.tfa.authenticatorSetupModal.invalidCodeError
        .visible
    )
    .ok('Should display invalid code error');

  const tfaSecret = await AccountSettingsSecurityPage.tfa.setupAuthenticatorTFA(
    t
  );

  await t
    .expect(
      AccountSettingsSecurityPage.tfa.authenticatorTfaEnabledToast.visible
    )
    .ok('TFA enabled message')
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
    .ok('TOTP TFA tab visible')
    .expect(VerificationPage.tabs.sms.title.visible)
    .notOk('SMS TFA tab visible');

  await VerificationPage.completeTotpChallenge(t, tfaSecret);
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.tfa.authenticatorTfaCheckbox)
    .click(AccountSettingsSecurityPage.tfa.disableModal.confirmButton)
    .expect(
      AccountSettingsSecurityPage.tfa.authenticatorTfaDisabledToast.visible
    )
    .ok('Should display message that TOTP TFA is disabled');
});

test('[TFA](TOTP): Should be able to complete full TFA flow', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?redirect=%2F`);
  const { email, password } = SignUpPage.generateRandomCredentials();

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.tfa.authenticatorTfaCheckbox)
    .typeText(
      AccountSettingsSecurityPage.tfa.authenticatorSetupModal.codeInput,
      '111111'
    )
    .click(AccountSettingsSecurityPage.tfa.authenticatorSetupModal.submitButton)
    .expect(
      AccountSettingsSecurityPage.tfa.authenticatorSetupModal.invalidCodeError
        .visible
    )
    .ok('Should display invalid code error');

  const tfaSecret = await AccountSettingsSecurityPage.tfa.setupAuthenticatorTFA(
    t
  );
  await t
    .expect(
      AccountSettingsSecurityPage.tfa.authenticatorTfaEnabledToast.visible
    )
    .ok('TFA enabled message')
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.signOut);

  await LoginPage.login(t, { email, password })
    .expect(VerificationPage.message.visible)
    .ok('should display help message')
    .expect(getLocation())
    .eql(`${VerificationPage.path}?redirect=%2F`)
    .expect(VerificationPage.tabs.totp.title.visible)
    .ok('TOTP TFA tab visible')
    .expect(VerificationPage.tabs.sms.title.visible)
    .notOk('SMS TFA tab visible');

  await VerificationPage.completeTotpChallenge(t, tfaSecret);
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.tfa.authenticatorTfaCheckbox)
    .click(AccountSettingsSecurityPage.tfa.disableModal.confirmButton)
    .expect(
      AccountSettingsSecurityPage.tfa.authenticatorTfaDisabledToast.visible
    )
    .ok('Should display message that TOTP TFA is disabled');
});

test('[TFA](SMS): User should be able to complete full TFA flow after password reset', async (t) => {
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
    .hover(AccountSettingsSecurityPage.tfa.textMessageCheckbox)
    .expect(
      AccountSettingsSecurityPage.tfa.textMessageDisabledTooltipText.visible
    )
    .ok('Should be disabled')
    .click(AccountSettingsSecurityPage.sidebar.details);

  await AccountSettingsDetailsPage.verifyCurrentPhoneNumber(t);

  await t
    .click(AccountSettingsSecurityPage.sidebar.security)
    .click(AccountSettingsSecurityPage.tfa.textMessageCheckbox)
    .typeText(
      AccountSettingsSecurityPage.tfa.authenticatorSetupModal.codeInput,
      '111111'
    )
    .click(AccountSettingsSecurityPage.tfa.authenticatorSetupModal.submitButton)
    .expect(
      AccountSettingsSecurityPage.tfa.authenticatorSetupModal.invalidCodeError
        .visible
    )
    .ok('Should display invalid code error');

  await AccountSettingsSecurityPage.tfa.setupTextMessageTFA(t);
  await t
    .expect(
      AccountSettingsSecurityPage.tfa.textMessageEnabledToastMessage.visible
    )
    .ok('TFA enabled message')
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
    .ok('SMS TFA tab visible')
    .expect(VerificationPage.tabs.totp.title.visible)
    .notOk('TOTP TFA tab not visible');

  await VerificationPage.completeSmsChallenge(t);
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security)
    .click(AccountSettingsSecurityPage.tfa.textMessageCheckbox)
    .click(AccountSettingsSecurityPage.tfa.disableModal.confirmButton)
    .expect(
      AccountSettingsSecurityPage.tfa.textMessageDisabledToastMessage.visible
    )
    .ok('Should display message that SMS TFA is disabled');
});

test('[TFA](SMS + TOTP): User should be able to complete full TFA flow', async (t) => {
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
    .click(AccountSettingsSecurityPage.tfa.textMessageCheckbox);

  await AccountSettingsSecurityPage.tfa.setupTextMessageTFA(t);

  await t.click(AccountSettingsSecurityPage.tfa.authenticatorTfaCheckbox);
  const tfaSecret = await AccountSettingsSecurityPage.tfa.setupAuthenticatorTFA(
    t
  );
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
  await VerificationPage.completeTotpChallenge(t, tfaSecret);
});
