import { ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractAccountSettingsPage } from './AbstractAccountSettingsPage';

export class AccountSettingsDetailsPage extends AbstractAccountSettingsPage {
  public readonly title = this.container.queryByText('Auth Tokens');
}

export default new AccountSettingsDetailsPage(
  ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE
);
