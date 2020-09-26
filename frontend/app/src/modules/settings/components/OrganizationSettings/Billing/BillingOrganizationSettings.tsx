import React, { useMemo, useState, useCallback } from 'react';
import { Card, StyledBody } from 'baseui/card';
import { Block } from 'baseui/block';
import YourPlan from 'modules/billing/components/YourPlan';
import { Modal } from 'baseui/modal';
import { addDays } from 'date-fns';
import useActivePlan from 'modules/billing/hooks/useActivePlan';
import type { PlanDTO, Subscription } from '@insight/types';
import useSubscriptions from 'modules/billing/hooks/useSubscriptions';
import { SubscriptionList } from 'modules/billing/components/SubscriptionList';
import { CheckoutForm } from 'modules/billing/components/CheckoutForm';
import { toaster } from 'baseui/toast';
import { SubscriptionDetailsContainer } from 'modules/billing/containers/SubscriptionDetails';

type Props = {
  organizationCreatedAt: Date | undefined;
};

const BillingOrganizationSettings = ({ organizationCreatedAt }: Props) => {
  const [selectedSubscription, setSelectedSubscription] = useState<
    Subscription
  >();

  const [isUpgrading, setIsUpgrading] = useState(false);
  const { subscriptions, revalidateSubscriptions } = useSubscriptions();
  const {
    plan,
    isLoading: isLoadingActivePlan,
    setActivePlan,
  } = useActivePlan();

  const onUpgradeClick = useCallback(() => setIsUpgrading(true), []);

  const resetsOn = useMemo(
    () =>
      organizationCreatedAt ? addDays(organizationCreatedAt, 30) : undefined,
    [organizationCreatedAt]
  );

  const onPlanUpgraded = useCallback(
    (upgradedPlan: PlanDTO) => {
      revalidateSubscriptions();
      setActivePlan(upgradedPlan);
      setIsUpgrading(false);
      toaster.positive(
        `Successfully upgraded to ${upgradedPlan.type} plan`,
        {}
      );
    },
    [setActivePlan, revalidateSubscriptions]
  );

  // TODO: this should be a seperate SSR route
  if (selectedSubscription) {
    return <SubscriptionDetailsContainer subscription={selectedSubscription} />;
  }

  return (
    <Block>
      <YourPlan
        sessionsUsed={0}
        plan={plan?.type}
        dataRetention={plan?.dataRetention}
        resetsOn={resetsOn}
        onUpgradeClick={onUpgradeClick}
        isLoading={isLoadingActivePlan}
      />
      {plan?.type !== 'enterprise' && (
        <Modal isOpen={isUpgrading} onClose={() => setIsUpgrading(false)}>
          <CheckoutForm onPlanUpgraded={onPlanUpgraded} />
        </Modal>
      )}

      <Card
        title="Subscriptions"
        overrides={{ Root: { style: { marginTop: '20px' } } }}
      >
        <StyledBody>
          <SubscriptionList
            subscriptions={subscriptions}
            onClick={setSelectedSubscription}
          />
        </StyledBody>
      </Card>
    </Block>
  );
};

export default React.memo(BillingOrganizationSettings);
