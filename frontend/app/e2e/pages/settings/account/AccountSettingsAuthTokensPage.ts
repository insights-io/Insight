import config from '../../../config';
import { ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractAccountSettingsPage } from './AbstractAccountSettingsPage';

export class AccountSettingsDetailsPage extends AbstractAccountSettingsPage {
  public readonly path = `${config.appBaseURL}/${ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE}`;
  public readonly title = this.container.queryByText('Auth Tokens');
}

export default new AccountSettingsDetailsPage();
