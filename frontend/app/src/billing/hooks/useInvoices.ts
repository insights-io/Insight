import { useQuery } from 'shared/hooks/useQuery';
import { useMemo } from 'react';
import { mapInvoice } from '@rebrowse/sdk';
import type { InvoiceDTO } from '@rebrowse/types';
import { client } from 'sdk';

export const cacheKey = (subscriptionId: string) => {
  return ['invoices', 'list', subscriptionId];
};

export const useInvoices = (
  subscriptionId: string,
  initialData: InvoiceDTO[]
) => {
  const { data } = useQuery(
    cacheKey(subscriptionId),
    () =>
      client.billing.invoices
        .listBySubscription(subscriptionId)
        .then((httpResponse) => httpResponse.data),
    { initialData: () => initialData }
  );

  const invoices = useMemo(() => {
    return (data || []).map(mapInvoice);
  }, [data]);

  return { invoices };
};
