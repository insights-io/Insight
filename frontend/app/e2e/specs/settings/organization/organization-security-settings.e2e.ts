import { queryByText } from '@testing-library/testcafe';

import {
  Sidebar,
  SignUpPage,
  OrganizationGeneralSettingsPage,
  OrganizationSecuritySettingsPage,
  AccountSettingsDetailsPage,
  AccountSettingsSecurityPage,
} from '../../../pages';

fixture('/settings/organization/security').page(
  OrganizationGeneralSettingsPage.path
);

test('[/settings/organization/security]: User should be able to set organization password policy which is enforced on password change and on team invite accept', async (t) => {
  const credentials = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, credentials);

  const {
    minCharactersInput,
    requireUpercaseCharacterCheckbox,
    requireLowercaseCharacterCheckbox,
    requireNumber,
    requireNonAlphanumericCharacter,
    saveButton,
    successMessage,
  } = OrganizationSecuritySettingsPage.passwordPolicy;

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
    .click(OrganizationGeneralSettingsPage.sidebar.security)
    .selectText(minCharactersInput)
    .pressKey('delete')
    .typeText(minCharactersInput, '15')
    .click(requireUpercaseCharacterCheckbox)
    .click(requireLowercaseCharacterCheckbox)
    .click(requireNumber)
    .click(requireNonAlphanumericCharacter)
    .click(saveButton)
    .expect(successMessage.visible)
    .ok('Success message is visible');

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings)
    .click(AccountSettingsDetailsPage.sidebar.security);

  const {
    changePassword: {
      changePassword,
      newPasswordInput,
      confirmNewPasswordInput,
      changePasswordButton,
      passwordChangedMessage,
    },
  } = AccountSettingsSecurityPage;

  await changePassword(t, {
    currentPassword: credentials.password,
    newPassword: 'longpassword',
    newPasswordConfirm: 'longpassword',
  })
    .expect(
      queryByText('Password should contain at least 15 characters').visible
    )
    .ok('Policy applied')
    .typeText(newPasswordInput, 'very')
    .typeText(confirmNewPasswordInput, 'very')
    .click(changePasswordButton)
    .expect(
      queryByText('Password should contain at least one uppercase character')
        .visible
    )
    .ok('Policy applied')
    .typeText(newPasswordInput, 'M')
    .typeText(confirmNewPasswordInput, 'M')
    .click(changePasswordButton)
    .expect(queryByText('Password should contain at least one number').visible)
    .ok('Policy applied')
    .typeText(newPasswordInput, '1')
    .typeText(confirmNewPasswordInput, '1')
    .click(changePasswordButton)
    .expect(
      queryByText(
        'Password should contain at least one non-alphanumeric character'
      ).visible
    )
    .ok('Policy applied')
    .typeText(newPasswordInput, '!')
    .typeText(confirmNewPasswordInput, '!')
    .click(changePasswordButton)
    .expect(passwordChangedMessage.visible)
    .ok('Password changed');

  // TODO: cover password reset
  // TODO: cover team invite accept
});
