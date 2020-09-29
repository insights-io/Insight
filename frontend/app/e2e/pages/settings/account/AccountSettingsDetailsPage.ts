import { queryByPlaceholderText, queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

import { ACCOUNT_SETTINGS_DETAILS_PAGE } from '../../../../src/shared/constants/routes';
import { VerificationPage } from '../..';

import { AbstractAccountSettingsPage } from './AbstractAccountSettingsPage';

export class AccountSettingsDetailsPage extends AbstractAccountSettingsPage {
  public readonly title = this.withinContainer.queryByText('Account details');

  public readonly fullName = this.withinContainer
    .queryByText('Full name')
    .parent()
    .child(1);

  public readonly email = this.withinContainer
    .queryByText('Email')
    .parent()
    .child(1);
  public readonly organizationId = this.withinContainer
    .queryByText('Organization ID')
    .parent()
    .child(1);

  public readonly memberSince = this.withinContainer
    .queryByText('Member since')
    .parent()
    .child(1);

  public readonly phoneNumber = this.withinContainer
    .queryByText('Phone number')
    .parent()
    .child(1)
    .child(0);

  public readonly phoneNumberConfigureButton = this.withinContainer
    .queryByText('Phone number')
    .parent()
    .child(1)
    .child(1);

  public readonly phoneNumberVerifiedMessage = queryByText(
    'Phone number verified'
  );

  public readonly phoneNumberCountryPicker = Selector('input')
    .withAttribute('aria-label', 'Select country')
    .parent()
    .parent();

  public readonly phoneNumberInput = queryByPlaceholderText('Phone number');
  public readonly phoneNumberNextStep = queryByText('Next');

  public completeSmsChallenge = (t: TestController) => {
    return VerificationPage.completeSmsChallenge(t);
  };

  public verifyCurrentPhoneNumber = async (t: TestController) => {
    await t
      .click(this.phoneNumberConfigureButton)
      .click(this.phoneNumberNextStep);

    await this.completeSmsChallenge(t);
    return t
      .expect(this.phoneNumberVerifiedMessage.visible)
      .ok('Success message');
  };
}

export default new AccountSettingsDetailsPage(ACCOUNT_SETTINGS_DETAILS_PAGE);
