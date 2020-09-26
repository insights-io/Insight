import React from 'react';
import { SubscriptionDetails } from 'modules/billing/components/SubscriptionDetails';
import useInvoices from 'modules/billing/hooks/useInvoices';
import type { Subscription, SubscriptionDTO } from '@insight/types';

type Props = {
  subscription: Subscription;
  onSubscriptionUpdated: (subscription: SubscriptionDTO) => void;
};

export const SubscriptionDetailsContainer = ({
  subscription,
  onSubscriptionUpdated,
}: Props) => {
  const { invoices } = useInvoices(subscription.id);

  return (
    <SubscriptionDetails
      subscription={subscription}
      onSubscriptionUpdated={onSubscriptionUpdated}
      invoices={invoices}
    />
  );
};
