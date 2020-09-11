import { queryByText } from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import {
  AccountSettingsPage,
  LoginPage,
  Sidebar,
  SignUpPage,
} from '../../pages';
import { getLocation } from '../../utils';

fixture('/account-settings/organization-settings').page(
  AccountSettingsPage.path
);

const {
  configurationEndpointInput,
  submitButton,
  nonBusinessEmailErrorMessage,
  setupCompleteMessage,
} = AccountSettingsPage.organizationSettings.tabs.security.ssoSetup;

test('User with non business email should not be able to setup SSO', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
  });

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

test('User with business email should be able to setup SAML SSO', async (t) => {
  const { password } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, {
    email: `${uuid()}@snuderls.eu`,
    password,
  });

  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.tabs.organizationSettings)
    .click(AccountSettingsPage.organizationSettings.tabs.security.button)
    .typeText(
      configurationEndpointInput,
      'https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata'
    )
    .click(submitButton)
    .expect(setupCompleteMessage.visible)
    .ok('SSO setup complete');

  // Is on okta page on SSO saml flow
  await Sidebar.signOut(t)
    .click(LoginPage.tabs.samlSso)
    .typeText(LoginPage.workEmailInput, 'matej.snuderl@snuderls.eu')
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      /^https:\/\/snuderls\.okta\.com\/login\/login\.htm\?fromURI=%2Fapp%2Fsnuderlsorg446661_insightdev_1%2Fexkw843tlucjMJ0kL4x6%2Fsso%2Fsaml%3FRelayState%3D(.*)http%253A%252F%252Flocalhost%253A3000%252F$/,
      'Is on okta page'
    );

  // Is on okta page even after normal login flow
  await t
    .navigateTo(AccountSettingsPage.path)
    .typeText(LoginPage.emailInput, 'matej.snuderl@snuderls.eu')
    .typeText(LoginPage.passwordInput, 'randomPassword')
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      /^https:\/\/snuderls\.okta\.com\/login\/login\.htm\?fromURI=%2Fapp%2Fsnuderlsorg446661_insightdev_1%2Fexkw843tlucjMJ0kL4x6%2Fsso%2Fsaml%3FRelayState%3D(.*)http%253A%252F%252Flocalhost%253A3000%252Faccount%252Fsettings$/,
      'Is on okta page'
    );
});