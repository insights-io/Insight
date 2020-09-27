import { ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

export class OrganizationSecurityAndPrivacySettingsPage extends AbstractOrganizationSettingsPage {
  public readonly header = this.container.queryByText('Security & Privacy');
}

export default new OrganizationSecurityAndPrivacySettingsPage(
  ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE
);
