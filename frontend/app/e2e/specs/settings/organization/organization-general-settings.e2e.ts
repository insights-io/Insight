import { queryByText } from '@testing-library/testcafe';

import {
  Sidebar,
  SignUpPage,
  OrganizationGeneralSettingsPage,
  LoginPage,
} from '../../../pages';

fixture('/settings/organization/general').page(
  OrganizationGeneralSettingsPage.path
);

test('[ORGANIZATION_GENERAL_SETTINGS]: User should be able to change general organization settings', async (t) => {
  const credentials = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, credentials);

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings);

  /* Display Name */
  await t
    .expect(OrganizationGeneralSettingsPage.nameInput.value)
    .eql('Insight', 'Should have Insight value by default')
    .selectText(OrganizationGeneralSettingsPage.nameInput)
    .pressKey('delete')
    .typeText(OrganizationGeneralSettingsPage.nameInput, 'Rebrowse')
    .pressKey('tab') // blur input
    .expect(
      queryByText(
        'Successfully changed organization name from "Insight" to "Rebrowse"'
      ).visible
    )
    .ok('Should change organization name')
    .expect(OrganizationGeneralSettingsPage.nameInput.value)
    .eql('Rebrowse', 'Input value updated');

  /* Default Role */
  await t
    .click(OrganizationGeneralSettingsPage.defaultRoleSelect)
    .click(queryByText('Admin'))
    .pressKey('tab') // blur input
    .expect(
      queryByText(
        'Successfully changed organization defaultRole from "member" to "admin"'
      ).visible
    )
    .ok('Should change default role');

  /* Open Membership */
  await t
    .click(OrganizationGeneralSettingsPage.openMembershipToggle)
    .expect(
      queryByText('Successfully enabled organization openMembership').visible
    )
    .ok('Should change open membership');

  // TODO: avatar

  /* Delete organization */
  await t
    .click(OrganizationGeneralSettingsPage.deleteOrganizationButton)
    .click(OrganizationGeneralSettingsPage.deleteOrganizationConfirmButton)
    .expect(OrganizationGeneralSettingsPage.organizationDeletedToast.visible)
    .ok('Organization is deleted');

  /* Login */
  await LoginPage.loginActions(t, credentials)
    .expect(LoginPage.errorMessages.invalidCredentials.visible)
    .ok('Cannot login anymore');
});
