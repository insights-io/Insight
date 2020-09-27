import config from '../../../config';
import { ORGANIZATION_SETTINGS_GENERAL_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

export class OrganizationGeneralSettingsPage extends AbstractOrganizationSettingsPage {
  public readonly path = `${config.appBaseURL}/${ORGANIZATION_SETTINGS_GENERAL_PAGE}`;
  public readonly header = this.container.queryByText('General');

  public readonly id = this.container.queryByText('ID').parent().child(1);
  public readonly name = this.container.queryByText('Name').parent().child(1);
  public readonly createdAt = this.container
    .queryByText('Created at')
    .parent()
    .child(1);
  public readonly updatedAt = this.container
    .queryByText('Updated at')
    .parent()
    .child(1);
}

export default new OrganizationGeneralSettingsPage();
