import { ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

export class OrganizationUsageAndPaymentsSettingsPage extends AbstractOrganizationSettingsPage {
  public readonly header = this.withinContainer.queryByText('Usage & Payments');
}

export default new OrganizationUsageAndPaymentsSettingsPage(
  ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE
);
