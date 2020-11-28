import { queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

import { ACCOUNT_SETTINGS_DETAILS_PAGE } from '../../../../src/shared/constants/routes';
import { VerificationPage } from '../..';

import { AbstractAccountSettingsPage } from './AbstractAccountSettingsPage';

export class AccountSettingsDetailsPage extends AbstractAccountSettingsPage {
  public readonly title = this.withinContainer.queryByText('Account details');

  public readonly fullNameInput = this.withinContainer.queryByPlaceholderText(
    'Full name'
  );
  public readonly emailInput = this.withinContainer.queryByPlaceholderText(
    'Email'
  );
  public readonly roleInput = this.withinContainer.queryByPlaceholderText(
    'Role'
  );
  public readonly memberSinceInput = this.withinContainer.queryByPlaceholderText(
    'Created at'
  );
  public readonly phoneNumberInput = this.withinContainer.queryByPlaceholderText(
    '51111222'
  );
  public readonly phoneNumberInputClear = this.withinContainer
    .queryByRole('button')
    .withAttribute('title', 'Clear value');

  public readonly phoneNumberVerifyButton = this.container
    .find('button')
    .withAttribute('aria-haspopup', 'true');

  public readonly phoneNumberVerifiedMessage = queryByText(
    'Phone number successfully verified'
  );

  public readonly phoneNumberCountryPicker = Selector('input')
    .withAttribute('aria-label', 'Select country')
    .parent()
    .parent();

  public readonly phoneNumberNextStep = queryByText('Continue');

  public completeSmsChallenge = (t: TestController) => {
    return VerificationPage.completeSmsChallenge(t);
  };

  public verifyCurrentPhoneNumber = async (t: TestController) => {
    await t.click(this.phoneNumberVerifyButton);
    await this.completeSmsChallenge(t);
    return t
      .expect(this.phoneNumberVerifiedMessage.visible)
      .ok('Success message');
  };
}

export default new AccountSettingsDetailsPage(ACCOUNT_SETTINGS_DETAILS_PAGE);
