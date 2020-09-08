import { queryByText } from '@testing-library/testcafe';

import { AccountSettingsPage, Sidebar, SignUpPage } from '../../pages';

fixture('/account-settings/organization-settings').page(
  AccountSettingsPage.path
);

test('User with non business email should not be able to setup SSO', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
  });

  const {
    configurationEndpointInput,
    submitButton,
    nonBusinessEmailErrorMessage,
  } = AccountSettingsPage.organizationSettings.tabs.security.ssoSetup;

  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.tabs.organizationSettings)
    .click(AccountSettingsPage.organizationSettings.tabs.security.button)
    .typeText(configurationEndpointInput, 'htqw')
    .click(submitButton)
    .expect(
      queryByText(
        'Cannot deserialize value of type `java.net.URL` from String "htqw": not a valid textual representation, problem: no protocol: htqw'
      ).visible
    )
    .ok('Validates URL')
    .selectText(configurationEndpointInput)
    .pressKey('delete')
    .typeText(configurationEndpointInput, 'https:///www.google.com')
    .click(submitButton)
    .expect(nonBusinessEmailErrorMessage.visible)
    .ok('Checks if work domain');
});
