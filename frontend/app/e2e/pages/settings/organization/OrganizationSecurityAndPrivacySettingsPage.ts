import config from '../../../config';
import { ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

export class OrganizationSecurityAndPrivacySettingsPage extends AbstractOrganizationSettingsPage {
  public readonly path = `${config.appBaseURL}/${ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE}`;
  public readonly header = this.container.queryByText('Security & Privacy');
}

export default new OrganizationSecurityAndPrivacySettingsPage();
