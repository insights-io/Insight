import { useQuery } from 'shared/hooks/useQuery';
import { useMemo } from 'react';
import { mapInvoice } from '@rebrowse/sdk';
import type { InvoiceDTO } from '@rebrowse/types';
import { client, INCLUDE_CREDENTIALS } from 'sdk';

export const cacheKey = (subscriptionId: string) => {
  return ['invoices', 'list', subscriptionId];
};

const queryFn = (subscriptionId: string) => {
  return client.billing.invoices
    .listBySubscription(subscriptionId, INCLUDE_CREDENTIALS)
    .then((httpResponse) => httpResponse.data);
};

export const useInvoices = (
  subscriptionId: string,
  initialData: InvoiceDTO[]
) => {
  const { data } = useQuery(
    cacheKey(subscriptionId),
    () => queryFn(subscriptionId),
    {
      initialData: () => initialData,
    }
  );

  const invoices = useMemo(() => {
    return (data || []).map(mapInvoice);
  }, [data]);

  return { invoices };
};
