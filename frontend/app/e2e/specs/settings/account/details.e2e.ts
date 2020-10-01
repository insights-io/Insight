import { queryByText } from '@testing-library/testcafe';

import {
  AccountSettingsDetailsPage,
  Sidebar,
  SignUpPage,
} from '../../../pages';

fixture('/settings/account/details').page(AccountSettingsDetailsPage.path);

test('[ACCOUNT-DETAILS]: As a user I want to be able to see details about my account & configure phone number', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
    company: 'My super company 2',
    fullName: 'Marko Skace',
  });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings);

  await t
    .expect(AccountSettingsDetailsPage.fullName.innerText)
    .eql('Marko Skace', 'Full name matches')
    .expect(AccountSettingsDetailsPage.email.innerText)
    .eql(email, 'Email matches')
    .click(AccountSettingsDetailsPage.phoneNumberConfigureButton)
    .click(AccountSettingsDetailsPage.phoneNumberCountryPicker)
    .typeText(AccountSettingsDetailsPage.phoneNumberCountryPicker, 'Slove')
    .click(queryByText('Slovenia (Slovenija)'))
    .typeText(AccountSettingsDetailsPage.phoneNumberInput, '51222333')
    .click(AccountSettingsDetailsPage.phoneNumberNextStep);

  await AccountSettingsDetailsPage.completeSmsChallenge(t);
  await t
    .expect(AccountSettingsDetailsPage.phoneNumberVerifiedMessage.visible)
    .ok('Success message is visible')
    .expect(AccountSettingsDetailsPage.phoneNumber.innerText)
    .eql('+38651222333', 'American phone number visible in the data table');
});
