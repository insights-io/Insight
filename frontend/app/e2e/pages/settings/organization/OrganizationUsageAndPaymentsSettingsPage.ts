import config from '../../../config';
import { ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

export class OrganizationUsageAndPaymentsSettingsPage extends AbstractOrganizationSettingsPage {
  public readonly path = `${config.appBaseURL}/${ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE}`;
  public readonly header = this.container.queryByText('Usage & Payments');
}

export default new OrganizationUsageAndPaymentsSettingsPage();
