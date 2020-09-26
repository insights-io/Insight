import { Subscription } from '@insight/types';
import { SubscriptionDetails } from 'modules/billing/components/SubscriptionDetails';
import useInvoices from 'modules/billing/hooks/useInvoices';
import React from 'react';

type Props = {
  subscription: Subscription;
};

export const SubscriptionDetailsContainer = ({ subscription }: Props) => {
  const { invoices } = useInvoices(subscription.id);

  return (
    <SubscriptionDetails subscription={subscription} invoices={invoices} />
  );
};
