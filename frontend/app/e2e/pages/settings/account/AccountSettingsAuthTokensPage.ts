import { queryByText } from '@testing-library/testcafe';

import { ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractAccountSettingsPage } from './AbstractAccountSettingsPage';

export class AccountSettingsDetailsPage extends AbstractAccountSettingsPage {
  public readonly title = this.withinContainer.queryByText('Auth Tokens');

  public readonly createNewButton = this.withinContainer.queryByText(
    'Create new'
  );

  public readonly revokeTooltipText = queryByText('Revoke');
  public readonly revokeConfirmButton = queryByText('Yes');

  private readonly table = this.container.find('div.auth-tokens');
  private readonly tableBody = this.table.child(1);

  public getTokenCount = () => {
    return this.tableBody.childElementCount;
  };

  public getToken = (n: number) => {
    return this.tableBody.child(n).child(0).innerText;
  };

  public revokeButton = (n: number) => {
    return this.tableBody.child(n).child(2).find('button');
  };
}

export default new AccountSettingsDetailsPage(
  ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE
);
