import { BillingApi } from 'api';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import { useMemo } from 'react';
import { mapInvoice } from '@insight/sdk';

const CACHE_KEY = 'BillingApi.invoices.list';

const useInvoices = (subscriptionId: string) => {
  const { data, isLoading, error } = useSWRQuery(CACHE_KEY, () =>
    BillingApi.invoices.list(subscriptionId)
  );

  const invoices = useMemo(() => {
    return (data || []).map(mapInvoice);
  }, [data]);

  return { invoices, isLoading, error };
};

export default useInvoices;
