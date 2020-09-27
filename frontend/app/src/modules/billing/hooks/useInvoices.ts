import { BillingApi } from 'api';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import { useMemo } from 'react';
import { mapInvoice } from '@insight/sdk';
import type { InvoiceDTO } from '@insight/types';

const CACHE_KEY = 'BillingApi.invoices.list';

const useInvoices = (subscriptionId: string, initialData: InvoiceDTO[]) => {
  const { data, error } = useSWRQuery(
    CACHE_KEY,
    () => BillingApi.invoices.listBySubscription(subscriptionId),
    { initialData }
  );

  const invoices = useMemo(() => {
    return (data || []).map(mapInvoice);
  }, [data]);

  return { invoices, error };
};

export default useInvoices;
