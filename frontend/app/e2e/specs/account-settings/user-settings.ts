import {
  queryByText,
  getByPlaceholderText,
  getByText,
} from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import config from '../../config';
import {
  AccountSettingsPage,
  LoginPage,
  Sidebar,
  SignUpPage,
  VerificationPage,
} from '../../pages';

fixture('/account-settings/user-settings').page(AccountSettingsPage.path);

test('Should be able to change password', async (t) => {
  const {
    password: initialPassword,
    email,
  } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password: initialPassword });

  const newPassword = uuid();
  const {
    currentPasswordInput,
    newPasswordInput,
    confirmNewPasswordInput,
    saveNewPasswordButton,
    newPasswordSameAsOldErrorMessage,
    passwordMissmatchErrorMessage,
    passwordChangedMessage,
  } = AccountSettingsPage.changePassword;

  // Try changing password to the same as current one
  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .typeText(currentPasswordInput, initialPassword)
    .typeText(newPasswordInput, initialPassword)
    .typeText(confirmNewPasswordInput, initialPassword)
    .click(saveNewPasswordButton)
    .expect(newPasswordSameAsOldErrorMessage.visible)
    .ok('Should not allow to change password to the same as previous one');

  // Try changing password with a wrong current one
  await t
    .selectText(currentPasswordInput)
    .pressKey('delete')
    .typeText(currentPasswordInput, uuid())
    .selectText(newPasswordInput)
    .pressKey('delete')
    .typeText(newPasswordInput, newPassword)
    .selectText(confirmNewPasswordInput)
    .pressKey('delete')
    .typeText(confirmNewPasswordInput, newPassword)
    .click(saveNewPasswordButton)
    .expect(passwordMissmatchErrorMessage.visible)
    .ok('Should not allow to change password if current one is wrong');

  // Change password to a new one
  await t
    .selectText(currentPasswordInput)
    .pressKey('delete')
    .typeText(currentPasswordInput, initialPassword)
    .click(saveNewPasswordButton)
    .expect(passwordChangedMessage.visible)
    .ok('Should display notification that password was changed');

  await Sidebar.signOut(t);
  await LoginPage.login(t, { email, password: newPassword })
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings);

  // Change password back to initial one
  await t
    .selectText(currentPasswordInput)
    .pressKey('delete')
    .typeText(currentPasswordInput, newPassword)
    .selectText(newPasswordInput)
    .pressKey('delete')
    .typeText(newPasswordInput, initialPassword)
    .selectText(confirmNewPasswordInput)
    .pressKey('delete')
    .typeText(confirmNewPasswordInput, initialPassword)
    .click(saveNewPasswordButton)
    .expect(passwordChangedMessage.visible)
    .ok('Should display notification that password was changed');
});

test('Should be able to invite new members to organization', async (t) => {
  await LoginPage.loginWithInsightUser(t);
  await t
    .click(AccountSettingsPage.tabs.organizationSettings)
    .expect(queryByText('000000').visible)
    .ok('Should display Insight organization id')
    .expect(queryByText('Insight').visible)
    .ok('Should display Insight organization name')
    .expect(queryByText(config.insightUserEmail).visible)
    .ok('Should display user email in the members table');

  const insightUserEmailSplit = config.insightUserEmail.split('@');
  const newMemberEmail = `${insightUserEmailSplit[0]}+${uuid()}@${
    insightUserEmailSplit[1]
  }`;

  await t
    .click(getByText('Invite new member'))
    .typeText(getByPlaceholderText('Email'), newMemberEmail)
    .click(getByText('Admin'))
    .click(getByText('Invite'))
    .expect(queryByText('Member invited').visible)
    .ok('Should display notification')
    .expect(queryByText(newMemberEmail).visible)
    .ok('Should display new member email in the team invites list');
});

test('Should be able to verify new phone number', async (t) => {
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
