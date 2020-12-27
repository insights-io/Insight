import { BillingApi } from 'api';
import { useQuery } from 'shared/hooks/useQuery';
import { useMemo } from 'react';
import { mapInvoice } from '@rebrowse/sdk';
import type { InvoiceDTO } from '@rebrowse/types';

export const cacheKey = (subscriptionId: string) => {
  return ['invoices', 'list', subscriptionId];
};

export const useInvoices = (
  subscriptionId: string,
  initialData: InvoiceDTO[]
) => {
  const { data } = useQuery(
    cacheKey(subscriptionId),
    () => BillingApi.invoices.listBySubscription(subscriptionId),
    { initialData: () => initialData }
  );

  const invoices = useMemo(() => {
    return (data || []).map(mapInvoice);
  }, [data]);

  return { invoices };
};
