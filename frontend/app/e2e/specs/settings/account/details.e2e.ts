import { queryByText } from '@testing-library/testcafe';

import {
  AccountSettingsDetailsPage,
  Sidebar,
  SignUpPage,
} from '../../../pages';

fixture('/settings/account/details').page(AccountSettingsDetailsPage.path);

test('As a user I want to change my full name and verify my new phone number', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  const fullName = 'Marko Skace';
  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
    company: 'My super company 2',
    fullName,
  });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings);

  await t
    .expect(AccountSettingsDetailsPage.fullNameInput.value)
    .eql(fullName, 'Full name matches')
    .typeText(AccountSettingsDetailsPage.fullNameInput, ' extra')
    .click(AccountSettingsDetailsPage.title) // blur input
    .expect(
      queryByText(
        `Successfully changed user full name from "${fullName}" to "${fullName} extra"`
      ).visible
    )
    .ok('Can change full name')
    .expect(AccountSettingsDetailsPage.emailInput.value)
    .eql(email, 'Email matches')
    .expect(AccountSettingsDetailsPage.roleInput.value)
    .eql('Admin', 'Role matches')
    .click(AccountSettingsDetailsPage.phoneNumberCountryPicker)
    .typeText(AccountSettingsDetailsPage.phoneNumberCountryPicker, 'Slove')
    .click(queryByText('Slovenia (Slovenija)'))
    .typeText(AccountSettingsDetailsPage.phoneNumberInput, '51222333')
    .click(AccountSettingsDetailsPage.title) // blur input
    .expect(
      queryByText('Successfully changed user phone number to "+38651222333"')
        .visible
    )
    .ok('Can change phone number')
    .click(AccountSettingsDetailsPage.phoneNumberVerifyButton);

  await AccountSettingsDetailsPage.completeSmsChallenge(t);
  await t
    .expect(AccountSettingsDetailsPage.phoneNumberVerifyButton.visible)
    .notOk(
      'Phone number verify button should not be visible after verification'
    )
    .click(AccountSettingsDetailsPage.phoneNumberInputClear)
    .click(AccountSettingsDetailsPage.title) // blur input
    .expect(queryByText('Successfully cleared user phone number').visible)
    .ok('Can clear phone number');
});
