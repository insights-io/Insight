import { queryByPlaceholderText, queryByText } from '@testing-library/testcafe';

import { ACCOUNT_SETTINGS_DETAILS_PAGE } from '../../../../src/shared/constants/routes';
import { VerificationPage } from '../..';

import { AbstractAccountSettingsPage } from './AbstractAccountSettingsPage';

export class AccountSettingsDetailsPage extends AbstractAccountSettingsPage {
  public readonly title = this.container.queryByText('Account details');

  public readonly fullName = this.container
    .queryByText('Full name')
    .parent()
    .child(1);

  public readonly email = this.container.queryByText('Email').parent().child(1);
  public readonly organizationId = this.container
    .queryByText('Organization ID')
    .parent()
    .child(1);

  public readonly memberSince = this.container
    .queryByText('Member since')
    .parent()
    .child(1);

  public readonly phoneNumber = this.container
    .queryByText('Phone number')
    .parent()
    .child(1)
    .child(0);

  public readonly phoneNumberConfigureButton = this.container
    .queryByText('Phone number')
    .parent()
    .child(1)
    .child(1);

  public readonly phoneNumberVerifiedMessage = queryByText(
    'Phone number verified'
  );

  public readonly phoneNumberInput = queryByPlaceholderText('Phone number');
  public readonly phoneNumberNextStep = queryByText('Next');

  public verifyCurrentPhoneNumber = async (t: TestController) => {
    await t.click(this.phoneNumberConfigureButton).click(queryByText('Next'));

    await VerificationPage.completeSmsChallenge(t);
    return t
      .expect(queryByText('Phone number verified').visible)
      .ok('Success message');
  };
}

export default new AccountSettingsDetailsPage(ACCOUNT_SETTINGS_DETAILS_PAGE);
