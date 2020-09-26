import { queryByText } from '@testing-library/testcafe';

export class InvoiceDetails {
  public readonly downloadButton = queryByText('Download as PDF…');
  public readonly downloadReceipt = queryByText('Download receipt');
}

export default new InvoiceDetails();
