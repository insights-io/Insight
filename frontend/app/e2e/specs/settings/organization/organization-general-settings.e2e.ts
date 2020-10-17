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
    .click(OrganizationGeneralSettingsPage.membershipSelect.admin)
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

  const iconBase64 =
    'data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAEgAAABICAYAAABV7bNHAAAEBklEQVR4nOycTSh8XRzHf2OGRkxmYmbhLbKhRLKxp3kWysLiSdnYILP1srKws7BWZKPslOIRWUhPSSyIvETMUF6SUebRoIw4zzn3P6b/nXuu47+Y8zvlfOo3zdyT7tenueeeOed2ssBMgNYArX9pxWiRH1Kx5P88kHTA5S9aYQXCYlc46cJEkFZcgXCqVDzpxMBP60KBUKoVc+J30pcQrb9Bk46XVpR10m3YSRSmzUFf/qNVgJ1EUR6ZIIKdQmXSx0GaNLQgAVqQAC1IgBYkQAsSoAUJ0IIEKCkoOzsbO0IKpQQFAgGYm5uDnZ0dCIVC2HFSYE8rGOVwOMjs7Cz55OPjg7S3t6PnUkZQMBg0pPzO8fEx8Xg82Nnw5bCilxbh0dHRgZpLiT7I6/VCU1MTt621tVVyGjNKCPL7/eDz+WzbMFFCUE5ODjidTm7b9fW15DRmlBAUj8fh9fWV27a4uCg5jRX0Dpp+g8je3p6lg56ZmSEulws7H74gVkNDQykxsViMjI6OErfbjZ7LBYowNTUFeXl5cHt7C6urqxAOh7EjGehJewFKdNIqowUJQO+DqquroaurC25ubmBychISiQR2JAtod4iamhpydXWVunuNjY2h37U4hXNiOnImKysrpnEPHTCSqqoqbCFqCGppaSHv7++WwWFfXx+2EFOhddI9PT2QlWU9PeuTVAJFUElJCdBvEMap/xgUQc3NzbbTG2wkrRIogr769kQiEYlJxEgXxJZ0GhsbuW1vb29wenoqOdHXSBdUXFwMZWVl3DZ2eZ2fn0tO9DXSBTE5+fn53LbDw0Nj8kwlpAsqLS0Fh8PBbdva2pKcRox0QXaT8HSMCBsbG7Z/5/F4jPki2UgXVFDAf6D2/v4eDg4OuG2VlZWwvr4Og4ODmYzGRbogt9vNPc76HyYpnfLycpifn4f6+nrjvWykC7Jb3tnd3bUcYx06k1NXV2d8vru7y2g2HtIF2S3vnJycmD5XVFTAwsICNDQ0pI5tbm5mNBsP6YIeHh64x4uKilLv2eW0vLxsksMErq2tZTxfOtJnFO1WSvv7+427FLsEu7u7TcIYIyMj8PT0JCOiBanzK7W1tSSRSHCf5LBjYmLi50yYscXAo6Ojb8thD1Xl5ub+HEGshoeHvyVnfHxchdVV+Sf1+Xxke3vbVszZ2Rnp7OzEFoMniBX9TUamp6fJ5eUliUajJBKJkKWlJdLb20sKCwuxpaQKfemZ/fRgzwe9vLzA8/MzZhQu6IJURy89C9CCBGhBArQgAVqQAC1IgBYkQAsSwAQ9YodQmEcmaB87hcLsM0H/YKdQGMON3mCJXxdJNwZ6iy5zmbbo+kRv8varuJu8faK3CUzbJvD/AAAA//90QVDAT2N5DgAAAABJRU5ErkJggg==';

  await t
    .click(OrganizationGeneralSettingsPage.avatarRadio.uploadAvatar)
    .click(OrganizationGeneralSettingsPage.saveAvatarButton)
    .expect(queryByText('Required').visible)
    .ok('Should upload the avatar before saving')
    .setFilesToUpload(OrganizationGeneralSettingsPage.uploadAvatarInput, [
      '../../../../public/static/icons/icon-72x72.png',
    ])
    .expect(Selector(`img[src="${iconBase64}"]`).visible)
    .ok('Logo visible')
    .click(OrganizationGeneralSettingsPage.saveAvatarButton)
    .expect(queryByText('Successfuly saved avatar preferences').visible)
    .ok('Avatar preferences saved')
    .expect(Sidebar.avatarRoot.find('img').getAttribute('src'))
    .eql(iconBase64, 'Should update the sidebar avatar');

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
