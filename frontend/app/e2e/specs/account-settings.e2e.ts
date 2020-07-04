import {
  queryByText,
  getByPlaceholderText,
  getByText,
} from '@testing-library/testcafe';
import { Selector } from 'testcafe';

import { login } from '../utils';
import config from '../config';

fixture('/account-settings').page(`${config.appBaseURL}/login`);

test('Should be able to change password', async (t) => {
  const newPassword = 'testPassword123456';

  await login(t, {
    email: config.insightUserEmail,
    password: config.insightUserPassword,
  });

  await t
    .click(Selector('svg[title="Menu"]'))
    .click(queryByText('Account settings'))
    .expect(queryByText(config.insightUserEmail).visible)
    .ok('Should display email address')
    .expect(queryByText('000000').visible)
    .ok('Should display Insight organization id');

  const newPasswordInput = getByPlaceholderText('New password');
  const confirmNewPasswordInput = getByPlaceholderText('Confirm new password');
  const saveNewPasswordButton = getByText('Save new password');

  // Try changing password to an existing one
  await t
    .typeText(newPasswordInput, config.insightUserPassword)
    .typeText(confirmNewPasswordInput, config.insightUserPassword)
    .click(saveNewPasswordButton)
    .expect(
      queryByText('New password cannot be the same as the previous one!')
        .visible
    )
    .ok(
      'Should not allow to change password to the same value as previous one'
    );

  // Change password to a new one
  await t
    .selectText(newPasswordInput)
    .pressKey('delete')
    .selectText(confirmNewPasswordInput)
    .pressKey('delete')
    .typeText(newPasswordInput, newPassword)
    .typeText(confirmNewPasswordInput, newPassword)
    .click(saveNewPasswordButton)
    .expect(queryByText('Password changed').visible)
    .ok('Should display notification that password was changed');

  // Change password back to initial one
  await t
    .selectText(newPasswordInput)
    .pressKey('delete')
    .selectText(confirmNewPasswordInput)
    .pressKey('delete')
    .typeText(newPasswordInput, config.insightUserPassword)
    .typeText(confirmNewPasswordInput, config.insightUserPassword)
    .click(saveNewPasswordButton);
});
