import { SubscriptionPlan } from '@insight/types';

export type BillingSubscriptionDTO = {
  id: string;
  plan: SubscriptionPlan;
  customerExternalId: string;
  customerInternalId: string;
  priceId: string;
  currentPeriodEnd: number;
  createdAt: string;
  cancelledAt?: string;
};

export type BillingSubscription = Omit<
  BillingSubscriptionDTO,
  'createdAt' | 'cancelledAt'
> & {
  createdAt: Date;
  cancelledAt?: Date;
};

export const mapBillingSubscriptip = (
  dto: BillingSubscriptionDTO
): BillingSubscription => {
  return {
    ...dto,
    createdAt: new Date(dto.createdAt),
    cancelledAt: dto.cancelledAt ? new Date(dto.cancelledAt) : undefined,
  };
};
