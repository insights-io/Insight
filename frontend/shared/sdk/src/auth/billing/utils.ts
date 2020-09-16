export type BillingSubscriptionDTO = {
  id: string;
  organizationId: string;
  priceId: string;
  currentPeriodEnd: number;
  createdAt: string;
};

export type BillingSubscription = Omit<BillingSubscriptionDTO, 'createdAt'> & {
  createdAt: Date;
};

export const mapBillingSubscriptip = (
  dto: BillingSubscriptionDTO
): BillingSubscription => {
  return { ...dto, createdAt: new Date(dto.createdAt) };
};
