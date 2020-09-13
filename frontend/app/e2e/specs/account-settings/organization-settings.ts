import { queryByText } from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import config from '../../config';
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
} = AccountSettingsPage.OrganizationSettings.tabs.security.sso;

test('[TEAM INVITE]: User should be able to invite new members to an organization', async (t) => {
  await LoginPage.loginWithInsightUser(t);
  await t
    .click(AccountSettingsPage.tabs.organizationSettings)
    .expect(queryByText('000000').visible)
    .ok('Should display Insight organization id')
    .expect(queryByText('Insight').visible)
    .ok('Should display Insight organization name')
    .expect(queryByText(config.insightUserEmail).visible)
    .ok('Should display user email in the members table');

  const insightUserEmailSplit = config.insightUserEmail.split('@');
  const newMemberEmail = `${insightUserEmailSplit[0]}+${uuid()}@${
    insightUserEmailSplit[1]
  }`;

  await t
    .click(AccountSettingsPage.TeamInvite.inviteNewMember)
    .typeText(AccountSettingsPage.TeamInvite.emailInput, newMemberEmail)
    .click(AccountSettingsPage.TeamInvite.role.admin)
    .click(AccountSettingsPage.TeamInvite.invite)
    .expect(AccountSettingsPage.TeamInvite.invitedMessage.visible)
    .ok('Should display notification')
    .expect(queryByText(newMemberEmail).visible)
    .ok('Should display new member email in the team invites list');
});

test('[SSO  SAML]: User with non-business email address should not be able to setup SAML SSO', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
  });

  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.tabs.organizationSettings)
    .click(AccountSettingsPage.OrganizationSettings.tabs.security.button)
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

test('[SSO SAML]: User with business email should be able to setup SAML SSO', async (t) => {
  const { email, password } = SignUpPage.generateRandomCredentialsForDomain(
    'snuderls.eu'
  );
  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
  });

  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.tabs.organizationSettings);

  await AccountSettingsPage.OrganizationSettings.tabs.security.sso.setup(t, {
    configurationEndpoint:
      'https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata',
  });

  // Is on okta page on SSO saml flow
  await Sidebar.signOut(t)
    .click(LoginPage.tabs.sso)
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

test('[SSO Google]: User with business email should be able to setup Google SSO', async (t) => {
  const domain = 'biz.only';
  const { email, password } = SignUpPage.generateRandomCredentialsForDomain(
    domain
  );
  const otherUser = `${uuid()}@${domain}`;

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.tabs.organizationSettings);

  await AccountSettingsPage.OrganizationSettings.tabs.security.sso.setup(t, {
    from: 'SAML',
    to: 'Google',
  });

  // Is on Google SSO flow
  await Sidebar.signOut(t)
    .click(LoginPage.tabs.sso)
    .typeText(LoginPage.workEmailInput, otherUser)
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      /^https:\/\/accounts\.google\.com\/o\/oauth2\/auth\/identifier\?client_id=237859759623-rfpiq8eo37afp0qc294ioqrjtq17q25h\.apps\.googleusercontent\.com&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fv1%2Fsso%2Foauth2%2Fgoogle%2Fcallback&response_type=code&scope=openid%20email%20profile&state=(.*)http%3A%2F%2Flocalhost%3A3000%2F&flowName=GeneralOAuthFlow$/,
      'Is on google page'
    );

  // Is on Google SSO flow after normal login
  await t
    .navigateTo(AccountSettingsPage.path)
    .typeText(LoginPage.emailInput, otherUser)
    .typeText(LoginPage.passwordInput, 'randomPassword')
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      /^https:\/\/accounts\.google\.com\/o\/oauth2\/auth\/identifier\?client_id=237859759623-rfpiq8eo37afp0qc294ioqrjtq17q25h\.apps\.googleusercontent\.com&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fv1%2Fsso%2Foauth2%2Fgoogle%2Fcallback&response_type=code&scope=openid%20email%20profile&state=(.*)http%3A%2F%2Flocalhost%3A3000%2Faccount%2Fsettings&flowName=GeneralOAuthFlow$/,
      'Is on google page'
    );
});

test('[SSO Microsoft]: User with business email should be able to setup Microsoft SSO', async (t) => {
  const domain = 'biz2.only';
  const { email, password } = SignUpPage.generateRandomCredentialsForDomain(
    domain
  );
  const otherUser = `${uuid()}@${domain}`;

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.tabs.organizationSettings);

  await AccountSettingsPage.OrganizationSettings.tabs.security.sso.setup(t, {
    from: 'SAML',
    to: 'Microsoft',
  });

  // Is on Microsoft SSO flow
  await Sidebar.signOut(t)
    .click(LoginPage.tabs.sso)
    .typeText(LoginPage.workEmailInput, otherUser)
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      /^https:\/\/login\.microsoftonline.com\/common\/oauth2\/v2\.0\/authorize\?client_id=783370b6-ee5d-47b5-bc12-2b9ebe4a4f1b&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fv1%2Fsso%2Foauth2%2Fmicrosoft%2Fcallback&response_type=code&scope=openid\+email\+profile&response_mode=query&state=(.*)http%3A%2F%2Flocalhost%3A3000%2F$/,
      'Is on microsoft page'
    );

  // Is on Microsoft SSO flow after normal login
  await t
    .navigateTo(AccountSettingsPage.path)
    .typeText(LoginPage.emailInput, otherUser)
    .typeText(LoginPage.passwordInput, 'randomPassword')
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      /^https:\/\/login\.microsoftonline.com\/common\/oauth2\/v2\.0\/authorize\?client_id=783370b6-ee5d-47b5-bc12-2b9ebe4a4f1b&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fv1%2Fsso%2Foauth2%2Fmicrosoft%2Fcallback&response_type=code&scope=openid\+email\+profile&response_mode=query&state=(.*)http%3A%2F%2Flocalhost%3A3000%2Faccount%2Fsettings$/,
      'Is on microsoft page'
    );
});

test('[SSO Github]: User with business email should be able to setup Github SSO', async (t) => {
  const domain = 'biz3.only';
  const { email, password } = SignUpPage.generateRandomCredentialsForDomain(
    domain
  );
  const otherUser = `${uuid()}@${domain}`;

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.accountSettings)
    .click(AccountSettingsPage.tabs.organizationSettings);

  await AccountSettingsPage.OrganizationSettings.tabs.security.sso.setup(t, {
    from: 'SAML',
    to: 'Github',
  });

  // Is on Github SSO flow
  await Sidebar.signOut(t)
    .click(LoginPage.tabs.sso)
    .typeText(LoginPage.workEmailInput, otherUser)
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      /^https:\/\/github\.com\/login\?client_id=210a475f7ac15d91bd3c&return_to=%2Flogin%2Foauth%2Fauthorize%3Fclient_id%3D210a475f7ac15d91bd3c%26redirect_uri%3Dhttp%253A%252F%252Flocalhost%253A8080%252Fv1%252Fsso%252Foauth2%252Fgithub%252Fcallback%26response_type%3Dcode%26scope%3Dread%253Auser%2Buser%253Aemail%26state(.*)http%253A%252F%252Flocalhost%253A3000%252F$/,
      'Is on github page'
    );

  // Is on Github SSO flow after normal login
  await t
    .navigateTo(AccountSettingsPage.path)
    .typeText(LoginPage.emailInput, otherUser)
    .typeText(LoginPage.passwordInput, 'randomPassword')
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      /^https:\/\/github\.com\/login\?client_id=210a475f7ac15d91bd3c&return_to=%2Flogin%2Foauth%2Fauthorize%3Fclient_id%3D210a475f7ac15d91bd3c%26redirect_uri%3Dhttp%253A%252F%252Flocalhost%253A8080%252Fv1%252Fsso%252Foauth2%252Fgithub%252Fcallback%26response_type%3Dcode%26scope%3Dread%253Auser%2Buser%253Aemail%26state(.*)http%253A%252F%252Flocalhost%253A3000%252Faccount%252Fsettings$/,
      'Is on github page'
    );
});