import { SubscriptionPlan } from '@insight/types';

export type BillingSubscriptionDTO = {
  id: string;
  plan: SubscriptionPlan;
  customerExternalId: string;
  customerInternalId: string;
  priceId: string;
  currentPeriodEnd: number;
  createdAt: string;
  canceledAt?: string;
};

export type BillingSubscription = Omit<
  BillingSubscriptionDTO,
  'createdAt' | 'canceledAt'
> & {
  createdAt: Date;
  canceledAt?: Date;
};

export const mapBillingSubscriptip = (
  dto: BillingSubscriptionDTO
): BillingSubscription => {
  return {
    ...dto,
    createdAt: new Date(dto.createdAt),
    canceledAt: dto.canceledAt ? new Date(dto.canceledAt) : undefined,
  };
};
