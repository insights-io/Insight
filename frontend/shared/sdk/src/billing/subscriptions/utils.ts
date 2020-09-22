import { BillingSubscription, BillingSubscriptionDTO } from '@insight/types';

export const mapBillingSubscriptip = (
  dto: BillingSubscriptionDTO
): BillingSubscription => {
  return {
    ...dto,
    createdAt: new Date(dto.createdAt),
    canceledAt: dto.canceledAt ? new Date(dto.canceledAt) : undefined,
  };
};
