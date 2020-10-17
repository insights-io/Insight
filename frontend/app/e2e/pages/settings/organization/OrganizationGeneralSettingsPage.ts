import { queryByText } from '@testing-library/testcafe';

import { ORGANIZATION_SETTINGS_GENERAL_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

export class OrganizationGeneralSettingsPage extends AbstractOrganizationSettingsPage {
  public readonly header = this.withinContainer.queryByText('General');

  public readonly idInput = this.withinContainer.queryByPlaceholderText(
    'Organization ID'
  );
  public readonly nameInput = this.withinContainer.queryByPlaceholderText(
    'Display name'
  );
  public readonly createdAtInput = this.withinContainer.queryByPlaceholderText(
    'Created at'
  );
  public readonly updatedAtInput = this.withinContainer.queryByPlaceholderText(
    'Updated at'
  );

  public readonly defaultRoleSelect = this.withinContainer.queryByText(
    'Member'
  );

  public readonly openMembershipToggle = this.container
    .find('input[name="openMembership"]')
    .parent();

  public readonly deleteOrganizationButton = this.withinContainer.queryByText(
    'Delete Organization'
  );
  public readonly deleteOrganizationConfirmButton = queryByText('Confirm');
  public readonly organizationDeletedToast = queryByText(
    'Organization deleted'
  );

  public readonly membershipSelect = {
    admin: queryByText('Admin'),
    member: queryByText('Member'),
  };

  public readonly avatarRadio = {
    useInitials: this.withinContainer.queryByText('Use initials'),
    uploadAvatar: this.withinContainer.queryByText('Upload avatar'),
  };

  public readonly uploadAvatarInput = this.container.find(
    'input[name="avatar"]'
  );
  public readonly uploadAvatarInputText = this.withinContainer.queryByText(
    'Browse files'
  );
  public readonly saveAvatarButton = this.withinContainer.queryByText(
    'Save Avatar'
  );
}

export default new OrganizationGeneralSettingsPage(
  ORGANIZATION_SETTINGS_GENERAL_PAGE
);
