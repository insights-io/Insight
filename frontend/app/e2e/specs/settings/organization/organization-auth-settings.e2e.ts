import {
  queryByDisplayValue,
  queryByTestId,
  queryByText,
} from '@testing-library/testcafe';
import { Selector } from 'testcafe';
import { v4 as uuid } from 'uuid';

import {
  LoginPage,
  Sidebar,
  SignUpPage,
  OrganizationGeneralSettingsPage,
  OrganizationSubscriptionSettingsPage,
  OrganizationAuthSettingsPage,
} from '../../../pages';
import { getLocation, getTitle } from '../../../utils';

fixture('/settings/organization/auth').page(
  OrganizationGeneralSettingsPage.path
);

test('As a user with non-business email I should not be able to setup SAML SSO', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
    .click(OrganizationGeneralSettingsPage.sidebar.auth)
    .click(OrganizationAuthSettingsPage.getToggleInput('Okta'))
    .typeText(OrganizationAuthSettingsPage.metadataInput, 'htqw')
    .click(OrganizationAuthSettingsPage.enableButton)
    .expect(
      queryByText(
        'Cannot deserialize value of type `java.net.URL` from String "htqw": not a valid textual representation, problem: no protocol: htqw'
      ).visible
    )
    .ok('Validates URL')
    .selectText(OrganizationAuthSettingsPage.metadataInput)
    .pressKey('delete')
    .typeText(
      OrganizationAuthSettingsPage.metadataInput,
      'https:///www.google.com'
    )
    .click(OrganizationAuthSettingsPage.enableButton)
    .expect(OrganizationAuthSettingsPage.nonBusinessEmailErrorMessage.visible)
    .ok('Checks if work domain');
});

test('As a user with business email, I should be able to setup SAML SSO', async (t) => {
  const { email, password } = SignUpPage.generateRandomCredentialsForDomain(
    'snuderls.eu'
  );
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
    .click(OrganizationGeneralSettingsPage.sidebar.auth);

  await OrganizationAuthSettingsPage.setupSso(t, {
    label: 'Okta',
    metadataEndpoint: OrganizationAuthSettingsPage.OKTA_METADATA_ENDPOINT,
  });

  const oktaDomain = 'snuderlstest';
  const oktaOrganization = 'snuderls-org-2948061';
  const oktaApp = `${oktaOrganization.split('-').join('')}_rebrowse_2`;

  // Is on okta page on SSO saml flow
  await Sidebar.signOut(t)
    .click(LoginPage.tabs.sso)
    .typeText(LoginPage.workEmailInput, 'matej.snuderl@snuderls.eu')
    .click(LoginPage.signInButton)
    .expect(getTitle())
    .eql(`${oktaOrganization} - Sign In`, 'Title indicates Okta page')
    .expect(getLocation())
    .match(
      new RegExp(
        `^https://${oktaDomain}.okta.com/login/login.htm\\?fromURI=%2Fapp%2F${oktaApp}%2FexkligrqDovHJsGmk5d5%2Fsso%2Fsaml%3FRelayState%3(.*)http%253A%252F%252Flocalhost%253A3000%252F$`
      ),
      'Location indicates Okta page'
    )
    .expect(Selector('input[name="username"]').visible)
    .ok('Has username input')
    .expect(Selector('input[name="password"]').visible)
    .ok('Has password input')
    .expect(queryByDisplayValue('Sign In').visible)
    .ok('Has Sign in button');

  // Is on okta page even after normal login flow
  await t
    .navigateTo(OrganizationAuthSettingsPage.path)
    .typeText(LoginPage.emailInput, 'matej.snuderl@snuderls.eu')
    .typeText(LoginPage.passwordInput, 'randomPassword')
    .click(LoginPage.signInButton)
    .expect(getTitle())
    .eql(`${oktaOrganization} - Sign In`, 'Title indicates Okta page')
    .expect(getLocation())
    .match(
      new RegExp(
        `^https://${oktaDomain}.okta.com/login/login.htm\\?fromURI=%2Fapp%2F${oktaApp}%2FexkligrqDovHJsGmk5d5%2Fsso%2Fsaml%3FRelayState%3(.*)http%253A%252F%252Flocalhost%253A3000%252Fsettings%252Forganization%252Fauth$`
      ),
      'Location indicates Okta page'
    )
    .expect(Selector('input[name="username"]').visible)
    .ok('Has username input')
    .expect(Selector('input[name="password"]').visible)
    .ok('Has password input')
    .expect(queryByDisplayValue('Sign In').visible)
    .ok('Has Sign in button');
});

test('As a user with business email should be able to setup Google SSO', async (t) => {
  const {
    email,
    password,
    domain,
  } = SignUpPage.generateRandomBussinessCredentials();
  const otherUser = `${uuid()}@${domain}`;
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
    .click(OrganizationGeneralSettingsPage.sidebar.auth);

  await OrganizationAuthSettingsPage.setupSso(t, {
    label: 'Google',
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
          OrganizationAuthSettingsPage.pathname
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

test('As a user with business email, I should be able to setup Microsoft SSO', async (t) => {
  const {
    email,
    password,
    domain,
  } = SignUpPage.generateRandomBussinessCredentials();
  const { email: otherUser } = SignUpPage.generateRandomCredentialsForDomain(
    domain
  );

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
    .click(OrganizationGeneralSettingsPage.sidebar.auth);

  await OrganizationAuthSettingsPage.setupSso(t, { label: 'Active directory' });

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
          OrganizationAuthSettingsPage.pathname
        )}$`
      ),
      'Is on microsoft page'
    )
    .expect(microsoftInput.value)
    .eql(otherUser, 'Should prefill user');
});

test('As a user with business email, I should be able to setup Github SSO', async (t) => {
  const {
    email,
    password,
    domain,
  } = SignUpPage.generateRandomBussinessCredentials();
  const { email: otherUser } = SignUpPage.generateRandomCredentialsForDomain(
    domain
  );

  await SignUpPage.signUpAndLogin(t, { email, password });
  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
    .click(OrganizationGeneralSettingsPage.sidebar.auth);

  await OrganizationAuthSettingsPage.setupSso(t, { label: 'Github' });

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
        )}%26redirect_uri%3Dhttp%253A%252F%252Flocalhost%253A8080%252Fv1%252Fsso%252Foauth2%252Fgithub%252Fcallback%26response_type%3Dcode%26scope%3Dread%253Auser%2Buser%253Aemail%26state(.*)http%253A%252F%252Flocalhost%253A3000${encodeURIComponent(
          encodeURIComponent(OrganizationAuthSettingsPage.pathname)
        )}$`
      ),
      'Is on github page'
    )
    .expect(githubLoginInput.value)
    .eql(otherUser, 'Should prefill user');
});

test('As a user, I should not be able to upgrade plan when already on Enterprise plan', async (t) => {
  await LoginPage.loginWithInsightUser(t)
    .click(OrganizationGeneralSettingsPage.sidebar.subscription)
    .expect(queryByText('Insight Enterprise').visible)
    .ok('Insight should be on enterprise plan')
    .expect(OrganizationSubscriptionSettingsPage.upgradeButton.visible)
    .notOk('Upgrade button is not visible');
});

test('As a user, I can subscribe using VISA card and then cancel my subscription', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
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
    .switchToIframe(checkoutForm.iframe.with({ timeout: 15000 }))
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
    .expect(queryByText('$15.00').with({ timeout: 3000 }).visible)
    .ok('Displays amount')
    .click(invoiceDetails.downloadButton)
    .click(invoiceDetails.downloadReceipt)
    .closeWindow();

  await t.click(OrganizationSubscriptionSettingsPage.sidebar.subscription);
  // eslint-disable-next-line no-restricted-globals
  await t.eval(() => location.reload());

  await t
    .expect(queryByText('Insight Free').visible)
    .ok('Should be back on Free plan');
});

test('As a user, I can subscribe using a 3DS payment method and then cancel my subscription', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
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
    .expect(queryByText('$15.00').with({ timeout: 3000 }).visible)
    .ok('Displays amount')
    .click(invoiceDetails.downloadButton)
    .click(invoiceDetails.downloadReceipt)
    .closeWindow();

  await t.click(OrganizationSubscriptionSettingsPage.sidebar.subscription);
  // eslint-disable-next-line no-restricted-globals
  await t.eval(() => location.reload());

  await t
    .expect(queryByText('Insight Free').visible)
    .ok('Should be back on Free plan');
});

test('As a user, I can recover and subscribe after failing to authenticate 3DS payment method', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
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
