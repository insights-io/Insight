import { queryByPlaceholderText, queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

class BillingSettings {
  public readonly tab = queryByText('Billing');
  public readonly formIframe = Selector('iframe');

  public readonly cardNumberInputElement = queryByPlaceholderText(
    'Card number'
  );

  public readonly exipiryInputElement = queryByPlaceholderText('MM / YY');
  public readonly cvcInputElement = queryByPlaceholderText('CVC');

  public readonly payButton = queryByText('Pay');
  public readonly paidMessage = queryByText('Subscription created.');
}

export default new BillingSettings();
