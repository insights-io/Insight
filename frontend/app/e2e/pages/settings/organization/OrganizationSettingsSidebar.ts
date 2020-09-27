import { AbstractSettingsSidebar } from '../AbstractSettingsSidebar';

export class OrganizationSettingsSidebar extends AbstractSettingsSidebar {
  public readonly generalSettings = this.container.queryByText(
    'General settings'
  );
  public readonly securityAndPrivacy = this.container.queryByText(
    'Security & Privacy'
  );
  public readonly members = this.container.queryByText('Members');
  public readonly auth = this.container.queryByText('Auth');

  public readonly subscription = this.container.queryByText('Subscription');
  public readonly usageAndPayments = this.container.queryByText(
    'Usage & Payments'
  );
}

export default new OrganizationSettingsSidebar();
