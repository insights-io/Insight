import { queryByTestId, queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';
import { v4 as uuid } from 'uuid';

import { ORGANIZATION_SETTINGS_GENERAL_PAGE } from '../../../src/shared/constants/routes';
import config from '../../config';
import {
  LoginPage,
  Sidebar,
  SignUpPage,
  OrganizationMembersSettingsPage,
  OrganizationGeneralSettingsPage,
  OrganizationSubscriptionSettingsPage,
  OrganizationAuthSettingsPage,
} from '../../pages';
import { getLocation } from '../../utils';

fixture('/settings/organization').page(ORGANIZATION_SETTINGS_GENERAL_PAGE);

test('[TEAM INVITE]: User should be able to invite new members to an organization', async (t) => {
  await LoginPage.loginWithInsightUser(t);
  await t
    .expect(OrganizationGeneralSettingsPage.id.value)
    .eql('000000', 'Should display Insight organization id')
    .expect(OrganizationGeneralSettingsPage.name.value)
    .eql('Insight', 'Should display Insight organization name');

  await t
    .click(OrganizationGeneralSettingsPage.sidebar.members)
    .expect(queryByText(config.insightUserEmail).visible)
    .ok('Should display user email in the members table');

  const insightUserEmailSplit = config.insightUserEmail.split('@');
  const newMemberEmail = `${insightUserEmailSplit[0]}+${uuid()}@${
    insightUserEmailSplit[1]
  }`;

  await t
    .click(OrganizationMembersSettingsPage.inviteNewTeamMemberButton)
    .typeText(
      OrganizationMembersSettingsPage.inviteNewMemberModal.emailInput,
      newMemberEmail
    )
    .click(OrganizationMembersSettingsPage.inviteNewMemberModal.role.admin)
    .click(OrganizationMembersSettingsPage.inviteNewMemberModal.inviteButton)
    .expect(
      OrganizationMembersSettingsPage.inviteNewMemberModal.invitedMessage
        .visible
    )
    .ok('Should display notification')
    .expect(queryByText(newMemberEmail).visible)
    .ok('Should display new member email in the team invites list');
});

test('[SSO  SAML]: User with non-business email address should not be able to setup SAML SSO', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.accountTab.trigger)
    .click(Sidebar.accountTab.menu.organizationSettings)
    .click(OrganizationGeneralSettingsPage.sidebar.securityAndPrivacy)
    .typeText(
      OrganizationAuthSettingsPage.ssoConfigurationEndpointInput,
      'htqw'
    )
    .click(OrganizationAuthSettingsPage.ssoSubmitButton)
    .expect(
      queryByText(
        'Cannot deserialize value of type `java.net.URL` from String "htqw": not a valid textual representation, problem: no protocol: htqw'
      ).visible
    )
    .ok('Validates URL')
    .selectText(OrganizationAuthSettingsPage.ssoConfigurationEndpointInput)
    .pressKey('delete')
    .typeText(
      OrganizationAuthSettingsPage.ssoConfigurationEndpointInput,
      'https:///www.google.com'
    )
    .click(OrganizationAuthSettingsPage.ssoSubmitButton)
    .expect(OrganizationAuthSettingsPage.nonBusinessEmailErrorMessage.visible)
    .ok('Checks if work domain');
});

test('[SSO SAML]: User with business email should be able to setup SAML SSO', async (t) => {
  const { email, password } = SignUpPage.generateRandomCredentialsForDomain(
    'snuderls.eu'
  );
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.accountTab.trigger)
    .click(Sidebar.accountTab.menu.organizationSettings);

  await OrganizationAuthSettingsPage.setupSso(t, {
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
    .navigateTo(OrganizationAuthSettingsPage.path)
    .typeText(LoginPage.emailInput, 'matej.snuderl@snuderls.eu')
    .typeText(LoginPage.passwordInput, 'randomPassword')
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      /^https:\/\/snuderls\.okta\.com\/login\/login\.htm\?fromURI=%2Fapp%2Fsnuderlsorg446661_insightdev_1%2Fexkw843tlucjMJ0kL4x6%2Fsso%2Fsaml%3FRelayState%3D(.*)http%253A%252F%252Flocalhost%253A3000%252Fsettings%252Fuser$/,
      'Is on okta page'
    );
});

test('[SSO Google]: User with business email should be able to setup Google SSO', async (t) => {
  const {
    email,
    password,
    domain,
  } = SignUpPage.generateRandomBussinessCredentials();
  const otherUser = `${uuid()}@${domain}`;
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.accountTab.trigger)
    .click(Sidebar.accountTab.menu.organizationSettings);

  await OrganizationAuthSettingsPage.setupSso(t, {
    from: 'SAML',
    to: 'Google',
  });

  // Is on Google SSO flow
  const googleInput = Selector('input[type="email"]').with({ timeout: 5000 });
  await Sidebar.signOut(t)
    .click(LoginPage.tabs.sso)
    .typeText(LoginPage.workEmailInput, otherUser)
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      new RegExp(
        `^https://accounts.google.com/o/oauth2/auth/identifier\\?client_id=237859759623-rfpiq8eo37afp0qc294ioqrjtq17q25h.apps.googleusercontent.com&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fv1%2Fsso%2Foauth2%2Fgoogle%2Fcallback&response_type=code&scope=openid%20email%20profile&login_hint=${encodeURIComponent(
          otherUser
        )}&state=(.*)http%3A%2F%2Flocalhost%3A3000%2F&flowName=GeneralOAuthFlow$`
      ),
      'Is on google page'
    )
    .expect(googleInput.value)
    .eql(
      '',
      'Does prefill user only if one of the known ones from account selector'
    );

  // Is on Google SSO flow after normal login
  await t
    .navigateTo(OrganizationAuthSettingsPage.path)
    .typeText(LoginPage.emailInput, otherUser)
    .typeText(LoginPage.passwordInput, 'randomPassword')
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      new RegExp(
        `^https://accounts.google.com/o/oauth2/auth/identifier\\?client_id=237859759623-rfpiq8eo37afp0qc294ioqrjtq17q25h.apps.googleusercontent.com&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fv1%2Fsso%2Foauth2%2Fgoogle%2Fcallback&response_type=code&scope=openid%20email%20profile&login_hint=${encodeURIComponent(
          otherUser
        )}&state=(.*)http%3A%2F%2Flocalhost%3A3000${encodeURIComponent(
          OrganizationAuthSettingsPage.relativePath
        )}&flowName=GeneralOAuthFlow$`
      ),
      'Is on google page'
    )
    .expect(googleInput.value)
    .eql(
      '',
      'Does prefill user only if one of the known ones from account selector'
    );
});

test('[SSO Microsoft]: User with business email should be able to setup Microsoft SSO', async (t) => {
  const {
    email,
    password,
    domain,
  } = SignUpPage.generateRandomBussinessCredentials();
  const otherUser = `${uuid()}@${domain}`;

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.accountTab.trigger)
    .click(Sidebar.accountTab.menu.organizationSettings);

  await OrganizationAuthSettingsPage.setupSso(t, {
    from: 'SAML',
    to: 'Microsoft',
  });

  // Is on Microsoft SSO flow
  const microsoftInput = Selector('input[type="email"]').with({
    timeout: 5000,
  });

  await Sidebar.signOut(t)
    .click(LoginPage.tabs.sso)
    .typeText(LoginPage.workEmailInput, otherUser)
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      new RegExp(
        `^https://login\\.microsoftonline\\.com/common/oauth2/v2\\.0/authorize\\?client_id=783370b6-ee5d-47b5-bc12-2b9ebe4a4f1b&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fv1%2Fsso%2Foauth2%2Fmicrosoft%2Fcallback&response_type=code&scope=openid\\+email\\+profile&response_mode=query&login_hint=${encodeURIComponent(
          otherUser
        )}&state=(.*)http%3A%2F%2Flocalhost%3A3000%2F$`
      ),
      'Is on microsoft page'
    )
    .expect(microsoftInput.value)
    .eql(otherUser, 'Should prefill user');

  // Is on Microsoft SSO flow after normal login
  await t
    .navigateTo(OrganizationAuthSettingsPage.path)
    .typeText(LoginPage.emailInput, otherUser)
    .typeText(LoginPage.passwordInput, 'randomPassword')
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      new RegExp(
        `^https://login\\.microsoftonline\\.com/common/oauth2/v2\\.0/authorize\\?client_id=783370b6-ee5d-47b5-bc12-2b9ebe4a4f1b&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fv1%2Fsso%2Foauth2%2Fmicrosoft%2Fcallback&response_type=code&scope=openid\\+email\\+profile&response_mode=query&login_hint=${encodeURIComponent(
          otherUser
        )}&state=(.*)http%3A%2F%2Flocalhost%3A3000${encodeURIComponent(
          OrganizationAuthSettingsPage.relativePath
        )}$`
      ),
      'Is on microsoft page'
    )
    .expect(microsoftInput.value)
    .eql(otherUser, 'Should prefill user');
});

test('[SSO Github]: User with business email should be able to setup Github SSO', async (t) => {
  const {
    email,
    password,
    domain,
  } = SignUpPage.generateRandomBussinessCredentials();
  const otherUser = `${uuid()}@${domain}`;

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.accountTab.trigger)
    .click(Sidebar.accountTab.menu.organizationSettings);

  await OrganizationAuthSettingsPage.setupSso(t, {
    from: 'SAML',
    to: 'Github',
  });

  // Is on Github SSO flow
  const githubLoginInput = Selector('input[name="login"]').with({
    timeout: 5000,
  });
  const login = encodeURIComponent(otherUser);
  await Sidebar.signOut(t)
    .click(LoginPage.tabs.sso)
    .typeText(LoginPage.workEmailInput, otherUser)
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      new RegExp(
        `^https://github\\.com/login\\?client_id=210a475f7ac15d91bd3c&login=${login}&return_to=%2Flogin%2Foauth%2Fauthorize%3Fclient_id%3D210a475f7ac15d91bd3c%26login%3D${encodeURIComponent(
          login
        )}%26redirect_uri%3Dhttp%253A%252F%252Flocalhost%253A8080%252Fv1%252Fsso%252Foauth2%252Fgithub%252Fcallback%26response_type%3Dcode%26scope%3Dread%253Auser%2Buser%253Aemail%26state(.*)http%253A%252F%252Flocalhost%253A3000%252F$`
      ),
      'Is on github page'
    )
    .expect(githubLoginInput.value)
    .eql(otherUser, 'Should prefill user');

  // Is on Github SSO flow after normal login

  await t
    .navigateTo(OrganizationAuthSettingsPage.path)
    .typeText(LoginPage.emailInput, otherUser)
    .typeText(LoginPage.passwordInput, 'randomPassword')
    .click(LoginPage.signInButton)
    .expect(getLocation())
    .match(
      new RegExp(
        `^https://github\\.com/login\\?client_id=210a475f7ac15d91bd3c&login=${login}&return_to=%2Flogin%2Foauth%2Fauthorize%3Fclient_id%3D210a475f7ac15d91bd3c%26login%3D${encodeURIComponent(
          login
        )}%26redirect_uri%3Dhttp%253A%252F%252Flocalhost%253A8080%252Fv1%252Fsso%252Foauth2%252Fgithub%252Fcallback%26response_type%3Dcode%26scope%3Dread%253Auser%2Buser%253Aemail%26state(.*)http%253A%252F%252Flocalhost%253A3000%252Fsettings%252Fuser$`
      ),
      'Is on github page'
    )
    .expect(githubLoginInput.value)
    .eql(otherUser, 'Should prefill user');
});

test('[BILLING]: Should not be able to upgrade plan on enterprise', async (t) => {
  await LoginPage.loginWithInsightUser(t)
    .click(OrganizationGeneralSettingsPage.sidebar.subscription)
    .expect(queryByText('Insight Enterprise').visible)
    .ok('Insight should be on enterprise plan')
    .expect(OrganizationSubscriptionSettingsPage.upgradeButton.visible)
    .notOk('Upgrade button is not visible');
});

test('[BILLING](VISA+CANCEL): As a user I can subscribe using VISA card and then cancel my subscription', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.accountTab.trigger)
    .click(Sidebar.accountTab.menu.organizationSettings)
    .click(OrganizationGeneralSettingsPage.sidebar.subscription);

  const {
    checkoutForm,
    planUpgradedToBusinessMessage,
    upgradeButton,
    invoiceDetails,
  } = OrganizationSubscriptionSettingsPage;

  await t
    .expect(queryByText('Insight Free').visible)
    .ok('Should have free plan by default')
    .click(upgradeButton)
    .switchToIframe(
      checkoutForm.iframe.with({
        timeout: 15000,
      })
    )
    .typeText(checkoutForm.cardNumberInputElement, '4242 4242 4242 4242')
    .typeText(checkoutForm.exipiryInputElement, '1044')
    .typeText(checkoutForm.cvcInputElement, '222')
    .switchToMainWindow()
    .click(checkoutForm.payButton)
    .expect(planUpgradedToBusinessMessage.with({ timeout: 15000 }).visible)
    .ok('Subscription should be created')
    .expect(queryByText('Insight Business').visible)
    .ok('Plan should be upgraded')
    .click(queryByText('Insight Business subscription'));

  /* Subscription details page */
  await t
    .click(queryByText('Invoices'))
    .expect(queryByText('Amount: 1500 usd').with({ timeout: 15000 }).visible)
    .ok('Displays amount due')
    .click(queryByText('Cancel'))
    .expect(queryByText('Successfully canceled subscription').visible)
    .ok('Should cancel the subscription')
    .expect(queryByText('Status: Canceled').visible)
    .ok('Subscription is canceled');

  /* External (Stripe) invoice details window */
  await t
    .click(queryByTestId('invoice-link'))
    .click(invoiceDetails.downloadButton)
    .click(invoiceDetails.downloadReceipt)
    .closeWindow();

  await t
    .click(OrganizationSubscriptionSettingsPage.sidebar.subscription)
    .expect(queryByText('Insight Free').visible)
    .ok('Should be back on Free plan');
});

test('[BILLING](3DS+CANCEL): As I user I can subscripe using a 3DS payment method and then cancel my subscription', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.accountTab.trigger)
    .click(Sidebar.accountTab.menu.organizationSettings)
    .click(OrganizationGeneralSettingsPage.sidebar.subscription);

  const {
    checkoutForm,
    planUpgradedToBusinessPropagationMessage,
    upgradeButton,
    invoiceDetails,
  } = OrganizationSubscriptionSettingsPage;

  await t
    .expect(queryByText('Insight Free').visible)
    .ok('Should have free plan by default')
    .click(upgradeButton)
    .switchToIframe(checkoutForm.iframe.with({ timeout: 15000 }))
    .typeText(checkoutForm.cardNumberInputElement, '4000 0000 0000 3220')
    .typeText(checkoutForm.exipiryInputElement, '1044')
    .typeText(checkoutForm.cvcInputElement, '222')
    .switchToMainWindow()
    .click(checkoutForm.payButton)
    .switchToIframe(
      checkoutForm.threedSecure.outerIframe.with({ timeout: 15000 })
    )
    .switchToIframe(
      checkoutForm.threedSecure.innerIframe.with({ timeout: 15000 })
    )
    .click(checkoutForm.threedSecure.completeButton.with({ timeout: 15000 }))
    .switchToMainWindow()
    .expect(
      planUpgradedToBusinessPropagationMessage.with({ timeout: 15000 }).visible
    )
    .ok('Subscription should be created')
    .expect(queryByText('Insight Business').with({ timeout: 15000 }).visible)
    .ok('Plan should be upgraded')
    .click(queryByText('Insight Business subscription'));

  /* Subscription details page */
  await t
    .click(queryByText('Invoices'))
    .expect(queryByText('Amount: 1500 usd').with({ timeout: 15000 }).visible)
    .ok('Displays amount due')
    .click(queryByText('Cancel'))
    .expect(queryByText('Successfully canceled subscription').visible)
    .ok('Should cancel the subscription')
    .expect(queryByText('Status: Canceled').visible)
    .ok('Subscription is canceled');

  /* External (Stripe) invoice details window */
  await t
    .click(queryByTestId('invoice-link'))
    .click(invoiceDetails.downloadButton)
    .click(invoiceDetails.downloadReceipt)
    .closeWindow();

  await t
    .click(OrganizationSubscriptionSettingsPage.sidebar.subscription)
    .expect(queryByText('Insight Free').visible)
    .ok('Should be back on Free plan');
});

test('[BILLING](3DS-FAILURE+RECOVER): As a user I can recover and subscribe after failing to authenticate 3DS payment method', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.accountTab.trigger)
    .click(Sidebar.accountTab.menu.organizationSettings)
    .click(OrganizationGeneralSettingsPage.sidebar.subscription);

  const {
    checkoutForm,
    planUpgradedToBusinessPropagationMessage,
    upgradeButton,
  } = OrganizationSubscriptionSettingsPage;

  await t
    .expect(queryByText('Insight Free').visible)
    .ok('Should have free plan by default')
    .click(upgradeButton)
    .switchToIframe(checkoutForm.iframe.with({ timeout: 15000 }))
    .typeText(checkoutForm.cardNumberInputElement, '4000 0000 0000 3220')
    .typeText(checkoutForm.exipiryInputElement, '1044')
    .typeText(checkoutForm.cvcInputElement, '222')
    .switchToMainWindow()
    .click(checkoutForm.payButton)
    .switchToIframe(
      checkoutForm.threedSecure.outerIframe.with({ timeout: 15000 })
    )
    .switchToIframe(
      checkoutForm.threedSecure.innerIframe.with({ timeout: 15000 })
    )
    .click(checkoutForm.threedSecure.failButton.with({ timeout: 15000 }))
    .switchToMainWindow()
    .expect(
      checkoutForm.threedSecure.failMessage.with({ timeout: 15000 }).visible
    )
    .ok('Should display reason for failure');

  await t
    .click(checkoutForm.payButton)
    .switchToIframe(
      checkoutForm.threedSecure.outerIframe.with({ timeout: 15000 })
    )
    .switchToIframe(
      checkoutForm.threedSecure.innerIframe.with({ timeout: 15000 })
    )
    .click(checkoutForm.threedSecure.completeButton.with({ timeout: 15000 }))
    .switchToMainWindow()
    .expect(
      planUpgradedToBusinessPropagationMessage.with({ timeout: 15000 }).visible
    )
    .ok('Subscription should be created');
});
