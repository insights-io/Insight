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
  await LoginPage.loginWithInsightUser(t);

  await t
    .expect(queryByText(config.insightUserEmail).visible)
    .ok('Should display email address')
    .expect(queryByText('000000').visible)
    .ok('Should display Insight organization id');

  const newPassword = 'testPassword123456';
  const currentPasswordInput = getByPlaceholderText('Current password');
  const newPasswordInput = getByPlaceholderText('New password');
  const confirmNewPasswordInput = getByPlaceholderText('Confirm new password');
  const saveNewPasswordButton = getByText('Save new password');

  // Try changing password to the same as current one
  await t
    .typeText(currentPasswordInput, config.insightUserPassword)
    .typeText(newPasswordInput, config.insightUserPassword)
    .typeText(confirmNewPasswordInput, config.insightUserPassword)
    .click(saveNewPasswordButton)
    .expect(
      queryByText('New password cannot be the same as the previous one!')
        .visible
    )
    .ok('Should not allow to change password to the same as previous one');

  // Try changing password with a wrong current one
  await t
    .selectText(currentPasswordInput)
    .pressKey('delete')
    .typeText(currentPasswordInput, 'password 12345')
    .selectText(newPasswordInput)
    .pressKey('delete')
    .typeText(newPasswordInput, config.insightUserPassword)
    .selectText(confirmNewPasswordInput)
    .pressKey('delete')
    .typeText(confirmNewPasswordInput, config.insightUserPassword)
    .click(saveNewPasswordButton)
    .expect(queryByText('Current password miss match').visible)
    .ok('Should not allow to change password if current one is wrong');

  // Change password to a new one
  await t
    .selectText(currentPasswordInput)
    .pressKey('delete')
    .typeText(currentPasswordInput, config.insightUserPassword)
    .selectText(newPasswordInput)
    .pressKey('delete')
    .typeText(newPasswordInput, newPassword)
    .selectText(confirmNewPasswordInput)
    .pressKey('delete')
    .typeText(confirmNewPasswordInput, newPassword)
    .click(saveNewPasswordButton)
    .expect(queryByText('Password changed').visible)
    .ok('Should display notification that password was changed');

  // Change password back to initial one
  await t
    .selectText(currentPasswordInput)
    .pressKey('delete')
    .typeText(currentPasswordInput, newPassword)
    .selectText(newPasswordInput)
    .pressKey('delete')
    .typeText(newPasswordInput, config.insightUserPassword)
    .selectText(confirmNewPasswordInput)
    .pressKey('delete')
    .typeText(confirmNewPasswordInput, config.insightUserPassword)
    .click(saveNewPasswordButton)
    .expect(queryByText('Password changed').visible)
    .ok('Should display notification that password was changed');

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
