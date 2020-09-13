import { queryByText } from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import {
  AccountSettingsPage,
  LoginPage,
  Sidebar,
  SignUpPage,
  VerificationPage,
} from '../../pages';

fixture('/account-settings/user-settings').page(AccountSettingsPage.path);

test('[CHANGE-PASSWORD]: User should be able to change its password', async (t) => {
  const {
    password: currentPassword,
    email,
  } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password: currentPassword });
  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings);

  const newPassword = uuid();
  const {
    currentPasswordInput,
    saveNewPasswordButton,
    newPasswordSameAsOldErrorMessage,
    passwordMissmatchErrorMessage,
    passwordChangedMessage,
  } = AccountSettingsPage.ChangePassword;

  // ERROR: Try changing password to the same as current one
  await AccountSettingsPage.ChangePassword.changePassword(t, {
    currentPassword,
    newPassword: currentPassword,
    newPasswordConfirm: currentPassword,
  })
    .expect(newPasswordSameAsOldErrorMessage.visible)
    .ok('Should not allow to change password to the same as previous one');

  // ERROR: Try changing password with a wrong current one
  await AccountSettingsPage.ChangePassword.clearInputs(t);
  await AccountSettingsPage.ChangePassword.changePassword(t, {
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
    .click(saveNewPasswordButton)
    .expect(passwordChangedMessage.visible)
    .ok('Should display notification that password was changed');

  await Sidebar.signOut(t);
  await LoginPage.login(t, { email, password: newPassword })
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings);

  // SUCCESS: Change password back to initial one
  await AccountSettingsPage.ChangePassword.changePassword(t, {
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
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.phoneNumber.configureButton)
    .typeText(AccountSettingsPage.phoneNumber.input, '51222333')
    .click(AccountSettingsPage.phoneNumber.nextStep);

  await VerificationPage.completeSmsChallenge(t);
  await t
    .expect(AccountSettingsPage.phoneNumber.verifiedMessage.visible)
    .ok('Success message is visible')
    .expect(queryByText('+151222333').visible)
    .ok('American phone number visible in the data table');
});
