import { queryByText } from '@testing-library/testcafe';

import { ORGANIZATION_SETTINGS_SECURITY_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

export class OrganizationSecuritySettingsPage extends AbstractOrganizationSettingsPage {
  public readonly header = this.withinContainer.queryByText('Security');

  public readonly enforceMfaToggle = this.container
    .find('input[name="enforceMfa"]')
    .parent();

  public readonly enforceMfaEnabledMessage = queryByText(
    'Successfully enabled organization enforce multi factor authentication'
  );

  public readonly passwordPolicy = {
    minCharactersInput: this.withinContainer.queryByPlaceholderText(
      'Min characters'
    ),
    preventPasswordReuseCheckbox: this.withinContainer.queryByText(
      'Prevent password reuse'
    ),
    requireLowercaseCharacterCheckbox: this.withinContainer.queryByText(
      'Require at least one lowercase letter from Latin alphabet (a-z)'
    ),
    requireUpercaseCharacterCheckbox: this.withinContainer.queryByText(
      'Require at least one uppercase letter from Latin alphabet (A-Z)'
    ),
    requireNumber: this.withinContainer.queryByText(
      'Require at least one number'
    ),
    requireNonAlphanumericCharacter: this.withinContainer.queryByText(
      "Require at least one non-alphanumeric character (! @ # $ % ^ & * () _ + - = [ ] {} | ')"
    ),
    saveButton: this.withinContainer.queryByText('Save'),
    successMessage: queryByText('Password policy updated'),
  };
}

export default new OrganizationSecuritySettingsPage(
  ORGANIZATION_SETTINGS_SECURITY_PAGE
);
