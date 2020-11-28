import { queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

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
    .eql('Rebrowse', 'Should have Rebrowse value by default')
    .selectText(OrganizationGeneralSettingsPage.nameInput)
    .pressKey('delete')
    .typeText(OrganizationGeneralSettingsPage.nameInput, 'Google')
    .pressKey('tab') // blur input
    .expect(
      queryByText(
        'Successfully changed organization name from "Rebrowse" to "Google"'
      ).visible
    )
    .ok('Should change organization name')
    .expect(OrganizationGeneralSettingsPage.nameInput.value)
    .eql('Google', 'Input value updated');

  /* Default Role */
  await t
    .click(OrganizationGeneralSettingsPage.defaultRoleSelect)
    .click(OrganizationGeneralSettingsPage.membershipSelect.admin)
    .pressKey('tab') // blur input
    .expect(
      queryByText(
        'Successfully changed organization default role from "member" to "admin"'
      ).visible
    )
    .ok('Should change default role');

  /* Open Membership */
  await t
    .click(OrganizationGeneralSettingsPage.openMembershipToggle)
    .expect(
      queryByText('Successfully enabled organization open membership').visible
    )
    .ok('Should change open membership');

  const smallIconPath = '../../../../public/assets/icons/icon-72x72.png';
  const largeIconPath = '../../../../public/assets/icons/icon-512x512.png';

  await t
    .click(OrganizationGeneralSettingsPage.avatarRadio.uploadAvatar)
    .click(OrganizationGeneralSettingsPage.saveAvatarButton)
    .expect(queryByText('Required').visible)
    .ok('Should upload the avatar before saving')
    .setFilesToUpload(OrganizationGeneralSettingsPage.uploadAvatarInput, [
      smallIconPath,
    ])
    .expect(Selector(`img[src="${smallLogoBase64}"]`).visible)
    .ok('Logo visible')
    .click(OrganizationGeneralSettingsPage.saveAvatarButton)
    .expect(
      queryByText('Please upload an image larger or equal than 258px by 258px')
        .visible
    )
    .ok('Avatar should be large enough')
    .setFilesToUpload(OrganizationGeneralSettingsPage.uploadAvatarInput, [
      largeIconPath,
    ])
    .expect(Selector(`img[src="${largeLogoBase64}"]`).visible)
    .ok('Logo visible')
    .click(OrganizationGeneralSettingsPage.saveAvatarButton)
    .expect(queryByText('Successfuly saved avatar preferences').visible)
    .ok('Avatar preferences saved')
    .expect(Sidebar.avatarRoot.find('img').getAttribute('src'))
    .eql(largeLogoBase64, 'Should update the sidebar avatar');

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

// TODO: don't use hardcoded base64
const smallLogoBase64 =
  'data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAEgAAABICAQAAAD/5HvMAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQfkCxwNLiONpsEPAAACAklEQVRo3u3avWsUYRDH8Z9oQkSDitw1vqOCopB/IJXIpVMIiAHBSvAFYnWIRYSI4gnaWAQsRES7g5SBNEJILRIFtTASVCw04AUPBAPe1yLnZhYDggs7gzzzVLtP8ymenXlmGWklqqprRi3hsFqaUV1VmRjSvAvFrnkN/ebU1HbnINRWTZIqWgjBQWhBFakehoNQXZoNBZqVlkKBlhSKg0igBEqgBPqPQD2RQFUmecmlKKB1NAHoMBwDVKPDSryhPwJoktUY8Qdt5ZMBPfEHHeS7AU37g47ww4Ae+IN2882ATvqDepkzJ2hDhK/sCgAtGvTFyEPbuM4FDqTimkD/tA5xm1F6o4AO8xGAOzFA65nu5p82+yOAjvMzS4kXI4CapmTc8wft4Gss0FlsXPUHPc6BTnmDenhlOMsMeIP25G5B74tf7ouCBrNOA2DKPzGO5E7QNX/QqOF0OJbb62dT+aAxA/pMxezsY47x8kE3Dehp7sr/AnhYPqhhQHezt7t4DkCjfNC4AZ3rvtvb5cCJ8kGX/ygbA7zOfjlsLh80bECL3OAWi9nzaY/P/ijLrB33ffJQX66WrUaTjV7VfmwNzkTx3rVIv/osh3nLGe8r7E4e8YEvvGOK82yP0ZdtoVKkcqVWOoESKIES6O+gcKMW4YZRwo3rhBtoCjfyFXAoLtTY4C+5XuzhLppMuAAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAyMC0xMS0yOFQxMzo0NjozNCswMDowMP70f+kAAAAldEVYdGRhdGU6bW9kaWZ5ADIwMjAtMTEtMjhUMTM6NDY6MzQrMDA6MDCPqcdVAAAAAElFTkSuQmCC';

const largeLogoBase64 =
  'data:image/jpeg;base64,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';
