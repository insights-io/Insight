import { AbstractSettingsSidebar } from '../AbstractSettingsSidebar';

export class OrganizationSettingsSidebar extends AbstractSettingsSidebar {
  public readonly generalSettings = this.withinContainer.queryByText(
    'General settings'
  );
  public readonly securityAndPrivacy = this.withinContainer.queryByText(
    'Security & Privacy'
  );
  public readonly members = this.withinContainer.queryByText('Members');
  public readonly auth = this.withinContainer.queryByText('Auth');

  public readonly subscription = this.withinContainer.queryByText(
    'Subscription'
  );
  public readonly usageAndPayments = this.withinContainer.queryByText(
    'Usage & Payments'
  );
}

export default new OrganizationSettingsSidebar();
