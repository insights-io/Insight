import type { Invoice, InvoiceDTO } from '@rebrowse/types';

export const mapInvoice = (invoice: InvoiceDTO | Invoice): Invoice => {
  return {
    ...invoice,
    createdAt: new Date(invoice.createdAt),
  };
};
