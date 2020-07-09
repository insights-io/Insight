import {
  queryByText,
  getByPlaceholderText,
  getByText,
} from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import { login } from '../utils';
import config from '../config';

fixture('/account-settings').page(`${config.appBaseURL}/account/settings`);

test('Should be able to change password', async (t) => {
  await login(t, {
    email: config.insightUserEmail,
    password: config.insightUserPassword,
  });

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
    .click(getByText('Organization settings'))
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
