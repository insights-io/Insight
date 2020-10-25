import { queryByText } from '@testing-library/testcafe';

import {
  Sidebar,
  SignUpPage,
  OrganizationGeneralSettingsPage,
  OrganizationSecuritySettingsPage,
  AccountSettingsDetailsPage,
  AccountSettingsSecurityPage,
  OrganizationMembersSettingsPage,
  AcceptTeamInvitePage,
} from '../../../pages';
import { findLinkFromDockerLogs } from '../../../utils';

fixture('/settings/organization/security').page(
  OrganizationGeneralSettingsPage.path
);

test('[/settings/organization/security]: As a user I should be able to change my password but be enforced to comply with the organization password policy', async (t) => {
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
});

test('[/settings/organization/security]: As a user I should be able to accept a team invite but be enforced to comply with the organization password policy', async (t) => {
  const credentials = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, credentials);

  const {
    minCharactersInput,
    successMessage,
    saveButton,
  } = OrganizationSecuritySettingsPage.passwordPolicy;

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
    .click(OrganizationGeneralSettingsPage.sidebar.security)
    .selectText(minCharactersInput)
    .pressKey('delete')
    .typeText(minCharactersInput, '15')
    .click(saveButton)
    .expect(successMessage.visible)
    .ok('Success message is visible');

  const { email: newMemberEmail } = SignUpPage.generateRandomCredentials();
  await t
    .click(OrganizationSecuritySettingsPage.sidebar.members)
    .click(OrganizationMembersSettingsPage.inviteNewTeamMemberButton)
    .typeText(
      OrganizationMembersSettingsPage.inviteNewMemberModal.emailInput,
      credentials.email
    )
    .click(OrganizationMembersSettingsPage.inviteNewMemberModal.role.admin)
    .click(OrganizationMembersSettingsPage.inviteNewMemberModal.inviteButton)
    .expect(
      queryByText('User with provided email is already in your organization')
        .visible
    )
    .ok('Can not invite user that is already in the organization')
    .selectText(OrganizationMembersSettingsPage.inviteNewMemberModal.emailInput)
    .pressKey('delete')
    .typeText(
      OrganizationMembersSettingsPage.inviteNewMemberModal.emailInput,
      newMemberEmail
    )
    .click(OrganizationMembersSettingsPage.inviteNewMemberModal.inviteButton)
    .expect(
      OrganizationMembersSettingsPage.inviteNewMemberModal.invitedMessage
        .visible
    )
    .ok('Should display notification')
    .expect(queryByText(newMemberEmail).visible)
    .ok('Should display new member email in the team invites list');

  const acceptInviteLink = findLinkFromDockerLogs();

  await t
    .click(OrganizationMembersSettingsPage.inviteNewTeamMemberButton)
    .typeText(
      OrganizationMembersSettingsPage.inviteNewMemberModal.emailInput,
      newMemberEmail
    )
    .click(OrganizationMembersSettingsPage.inviteNewMemberModal.role.admin)
    .click(OrganizationMembersSettingsPage.inviteNewMemberModal.inviteButton)
    .expect(
      queryByText('User with provided email has an active outstanding invite')
        .visible
    )
    .ok('Inviting same user again should fail with nice error')
    .click(OrganizationMembersSettingsPage.inviteNewMemberModal.cancelButton);

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.signOut)
    .navigateTo(acceptInviteLink)
    .typeText(AcceptTeamInvitePage.fullNameInput, 'Bruce Lee')
    .typeText(AcceptTeamInvitePage.passwordInput, 'shortpassword')
    .click(AcceptTeamInvitePage.submitButton)
    .expect(
      queryByText('Password should contain at least 15 characters').visible
    )
    .ok('Organization password policy enforced')
    .typeText(AcceptTeamInvitePage.passwordInput, 'someExtraCharacters')
    .click(AcceptTeamInvitePage.submitButton)
    .click(Sidebar.banner.trigger)
    .expect(queryByText(newMemberEmail).visible)
    .ok('Newly created user is logged in');
});
