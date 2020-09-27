import { AbstractSettingsSidebar } from '../AbstractSettingsSidebar';

export class AccountSettingsSidebar extends AbstractSettingsSidebar {
  public readonly details = this.container.queryByText('Details');
  public readonly security = this.container.queryByText('Security');
  public readonly authTokens = this.container.queryByText('Auth Tokens');
}

export default new AccountSettingsSidebar();
