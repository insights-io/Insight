import { queryByPlaceholderText, queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

class BillingSettings {
  public readonly tab = queryByText('Billing');
  public readonly formIframe = Selector('iframe');

  public readonly threedSecure = {
    outerIframe: Selector(
      'iframe[src^="https://js.stripe.com/v3/three-ds-2-challenge"]'
    ),
    innerIframe: Selector('iframe[id="challengeFrame"]'),
    complete: queryByText('Complete'),
    fail: queryByText('Fail'),
    failMessage: queryByText(
      'We are unable to authenticate your payment method. Please choose a different payment method and try again.'
    ),
  };

  public readonly cardNumberInputElement = queryByPlaceholderText(
    'Card number'
  );

  public readonly upgradeButton = queryByText('Upgrade');

  public readonly exipiryInputElement = queryByPlaceholderText('MM / YY');
  public readonly cvcInputElement = queryByPlaceholderText('CVC');

  public readonly payButton = queryByText('Pay');
  public readonly paidMessage = queryByText('Subscription created');
}

export default new BillingSettings();
