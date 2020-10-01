import { v4 as uuid } from 'uuid';

import {
  AccountSettingsSecurityPage,
  AccountSettingsDetailsPage,
  Sidebar,
  SignUpPage,
  LoginPage,
} from '../../../pages';

fixture('/settings/account').page(AccountSettingsSecurityPage.path);

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
