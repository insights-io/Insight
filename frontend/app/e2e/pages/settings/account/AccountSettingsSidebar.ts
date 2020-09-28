import { AbstractSettingsSidebar } from '../AbstractSettingsSidebar';

export class AccountSettingsSidebar extends AbstractSettingsSidebar {
  public readonly details = this.withinContainer.queryByText('Details');
  public readonly security = this.withinContainer.queryByText('Security');
  public readonly authTokens = this.withinContainer.queryByText('Auth Tokens');
}

export default new AccountSettingsSidebar();
