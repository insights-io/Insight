import { queryByText } from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import {
  LoginPage,
  Sidebar,
  SignUpPage,
  VerificationPage,
  AccountSettingsDetailsPage,
  AccountSettingsSecurityPage,
} from '../../pages';

fixture('/settings/account').page(AccountSettingsDetailsPage.path);

test('[CHANGE-PASSWORD]: User should be able to change its password', async (t) => {
  const {
    password: currentPassword,
    email,
  } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password: currentPassword });
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security);

  const newPassword = uuid();
  const {
    currentPasswordInput,
    changePasswordButton,
    newPasswordSameAsOldErrorMessage,
    passwordMissmatchErrorMessage,
    passwordChangedMessage,
  } = AccountSettingsSecurityPage.changePassword;

  // ERROR: Try changing password to the same as current one
  await AccountSettingsSecurityPage.changePassword
    .changePassword(t, {
      currentPassword,
      newPassword: currentPassword,
      newPasswordConfirm: currentPassword,
    })
    .expect(newPasswordSameAsOldErrorMessage.visible)
    .ok('Should not allow to change password to the same as previous one');

  // ERROR: Try changing password with a wrong current one
  await AccountSettingsSecurityPage.changePassword.clearInputs(t);
  await AccountSettingsSecurityPage.changePassword
    .changePassword(t, {
      currentPassword: uuid(),
      newPassword,
      newPasswordConfirm: newPassword,
    })
    .expect(passwordMissmatchErrorMessage.visible)
    .ok('Should not allow to change password if current one is wrong');

  // SUCCESS: Change password to a new one
  await t
    .selectText(currentPasswordInput)
    .pressKey('delete')
    .typeText(currentPasswordInput, currentPassword)
    .click(changePasswordButton)
    .expect(passwordChangedMessage.visible)
    .ok('Should display notification that password was changed');

  await Sidebar.signOut(t);
  await LoginPage.login(t, { email, password: newPassword })
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security);

  // SUCCESS: Change password back to initial one
  await AccountSettingsSecurityPage.changePassword
    .changePassword(t, {
      currentPassword: newPassword,
      newPassword: currentPassword,
      newPasswordConfirm: currentPassword,
    })
    .expect(passwordChangedMessage.visible)
    .ok('Should display notification that password was changed');
});

test('[PHONE-NUMBER]: User should be able to set and verify a phone number', async (t) => {
  const { email, password } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.phoneNumberConfigureButton)
    .typeText(AccountSettingsDetailsPage.phoneNumberInput, '51222333')
    .click(AccountSettingsDetailsPage.phoneNumberNextStep);

  await VerificationPage.completeSmsChallenge(t);
  await t
    .expect(AccountSettingsDetailsPage.phoneNumberVerifiedMessage.visible)
    .ok('Success message is visible')
    .expect(queryByText('+151222333').visible)
    .ok('American phone number visible in the data table');
});
