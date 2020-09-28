import { ORGANIZATION_SETTINGS_GENERAL_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

export class OrganizationGeneralSettingsPage extends AbstractOrganizationSettingsPage {
  public readonly header = this.withinContainer.queryByText('General');

  public readonly id = this.withinContainer.queryByText('ID').parent().child(1);
  public readonly name = this.withinContainer
    .queryByText('Name')
    .parent()
    .child(1);

  public readonly createdAt = this.withinContainer
    .queryByText('Created at')
    .parent()
    .child(1);

  public readonly updatedAt = this.withinContainer
    .queryByText('Updated at')
    .parent()
    .child(1);
}

export default new OrganizationGeneralSettingsPage(
  ORGANIZATION_SETTINGS_GENERAL_PAGE
);
