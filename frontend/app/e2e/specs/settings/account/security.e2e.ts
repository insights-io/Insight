import { v4 as uuid } from 'uuid';

import {
  AccountSettingsSecurityPage,
  AccountSettingsDetailsPage,
  Sidebar,
  SignUpPage,
  LoginPage,
} from '../../../pages';
import Login from '../../../pages/Login';

fixture('/settings/account/security').page(AccountSettingsSecurityPage.path);

test('As a user I want to change my password and be able to login with it', async (t) => {
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

  // ERROR: Cannot login with old password
  await LoginPage.loginActions(t, { email, password: currentPassword })
    .expect(Login.errorMessages.invalidCredentials.visible)
    .ok('Should not be able to login with old password');

  await t
    .selectText(LoginPage.passwordInput)
    .pressKey('delete')
    .typeText(LoginPage.passwordInput, newPassword)
    .click(LoginPage.signInButton)
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security);
});
