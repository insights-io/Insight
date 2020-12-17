import {
  queryAllByText,
  queryByTestId,
  queryByText,
} from '@testing-library/testcafe';

import {
  Sidebar,
  SignUpPage,
  OrganizationGeneralSettingsPage,
  OrganizationSubscriptionSettingsPage,
} from '../../../pages';

fixture('/settings/organization/subscription').page(
  OrganizationGeneralSettingsPage.path
);

const {
  checkoutForm,
  planUpgradedToBusinessMessage,
  upgradeButton,
  planUpgradedToBusinessPropagationMessage,
  terminateButton,
  invoicesTab,
} = OrganizationSubscriptionSettingsPage;

test('As a user I can subscribe again after canceling my first subscription using VISA', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
    .click(OrganizationGeneralSettingsPage.sidebar.subscription);

  await t
    .expect(queryByText('Rebrowse Free').visible)
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
    .expect(queryByText('Rebrowse Business').visible)
    .ok('Plan should be upgraded')
    .click(queryByText('Rebrowse Business - ', { exact: false }))
    .expect(queryByText('Plan: Rebrowse Business').visible)
    .ok('Plan visible')
    .expect(queryByText('Status: Active').visible)
    .ok('Status visible')
    .click(invoicesTab.with({ timeout: 15000 }))
    .expect(queryByText('Amount: 1500 usd').visible)
    .ok('Displays amount due');

  /* External (Stripe) invoice details window */
  await t
    .click(queryByTestId('invoice-link'))
    .expect(queryAllByText('$15.00').with({ timeout: 3000 }).visible)
    .ok('Displays amount')
    .expect(queryByText('This is a test invoice.', { exact: false }).visible)
    .ok('Should be a test invoice');

  /* Stripe keeps breaking UI
    .click(invoiceDetails.downloadButton)
    .click(invoiceDetails.downloadReceipt);
  */

  await t
    .closeWindow()
    .click(terminateButton)
    .expect(queryByText('Successfully canceled subscription').visible)
    .ok('Should cancel the subscription')
    .expect(queryByText('Status: Canceled').visible)
    .ok('Subscription is canceled');

  await t
    .click(OrganizationSubscriptionSettingsPage.sidebar.subscription)
    .expect(queryByText('Rebrowse Free').with({ timeout: 15000 }).visible)
    .ok('Should be back on Free plan')
    .click(upgradeButton)
    .switchToIframe(checkoutForm.iframe.with({ timeout: 15000 }))
    .typeText(checkoutForm.cardNumberInputElement, '4242 4242 4242 4242')
    .typeText(checkoutForm.exipiryInputElement, '1044')
    .typeText(checkoutForm.cvcInputElement, '222')
    .switchToMainWindow()
    .click(checkoutForm.payButton)
    .expect(planUpgradedToBusinessMessage.with({ timeout: 15000 }).visible)
    .ok('Subscription should be created')
    .expect(queryAllByText('Rebrowse Business - ', { exact: false }).count)
    .eql(2, 'Plan should be upgraded')
    .expect(
      queryAllByText('Rebrowse Business - ', { exact: false })
        .nth(0)
        .parent()
        .parent()
        .parent()
        .find('svg')
        .getAttribute('title')
    )
    .eql('Check', 'First subscription should be the newly created one');
});

test('As a user, I can subscribe using a 3DS payment method and then cancel my subscription', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
    .click(OrganizationGeneralSettingsPage.sidebar.subscription);

  await t
    .expect(queryByText('Rebrowse Free').visible)
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
    .expect(queryByText('Rebrowse Business').with({ timeout: 15000 }).visible)
    .ok('Plan should be upgraded')
    .click(queryByText('Rebrowse Business - ', { exact: false }));

  /* Subscription details page */
  await t
    .click(invoicesTab.with({ timeout: 15000 }))
    .expect(queryByText('Amount: 1500 usd').visible)
    .ok('Displays amount due')
    .click(terminateButton)
    .expect(queryByText('Successfully canceled subscription').visible)
    .ok('Should cancel the subscription')
    .expect(queryByText('Status: Canceled').visible)
    .ok('Subscription is canceled');

  /* External (Stripe) invoice details window */
  await t
    .click(queryByTestId('invoice-link'))
    .expect(queryAllByText('$15.00').with({ timeout: 3000 }).visible)
    .ok('Displays amount')
    .expect(queryByText('This is a test invoice.', { exact: false }).visible)
    .ok('Should be a test invoice');

  /* Stripe keeps breaking UI
    .click(invoiceDetails.downloadButton)
    .click(invoiceDetails.downloadReceipt);
  */

  await t
    .closeWindow()
    .click(OrganizationSubscriptionSettingsPage.sidebar.subscription)
    .expect(queryByText('Rebrowse Free').visible)
    .ok('Should be back on Free plan');
});

test('As a user, I can recover and subscribe after failing to authenticate 3DS payment method', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.settings)
    .click(OrganizationGeneralSettingsPage.sidebar.subscription);

  await t
    .expect(queryByText('Rebrowse Free').visible)
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
