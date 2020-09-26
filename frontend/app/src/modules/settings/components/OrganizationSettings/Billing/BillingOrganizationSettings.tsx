import React, { useMemo, useState, useCallback } from 'react';
import { Card, StyledBody } from 'baseui/card';
import { Block } from 'baseui/block';
import YourPlan from 'modules/billing/components/YourPlan';
import { Modal } from 'baseui/modal';
import { addDays } from 'date-fns';
import useActivePlan from 'modules/billing/hooks/useActivePlan';
import useSubscriptions from 'modules/billing/hooks/useSubscriptions';
import { SubscriptionList } from 'modules/billing/components/SubscriptionList';
import { CheckoutForm } from 'modules/billing/components/CheckoutForm';
import { toaster } from 'baseui/toast';
import { SubscriptionDetailsContainer } from 'modules/billing/containers/SubscriptionDetails';
import type {
  PlanDTO,
  SubscriptionDTO,
  SubscriptionPlan,
} from '@insight/types';

type Props = {
  organizationCreatedAt: Date | undefined;
};

const BillingOrganizationSettings = ({ organizationCreatedAt }: Props) => {
  const [selectedSubscriptionId, setSelectedSubscriptionId] = useState<
    string
  >();

  const [isUpgrading, setIsUpgrading] = useState(false);
  const {
    subscriptions,
    revalidateSubscriptions,
    updateSubscription,
  } = useSubscriptions();
  const {
    plan,
    isLoading: isLoadingActivePlan,
    setActivePlan,
    revalidateActivePlan,
  } = useActivePlan();

  const selectedSubscription = useMemo(
    () => subscriptions.find((s) => s.id === selectedSubscriptionId),
    [selectedSubscriptionId, subscriptions]
  );

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
      toaster.positive(`Successfully upgraded to ${upgradedPlan.type} plan`, {
        autoHideDuration: 10000,
      });
    },
    [setActivePlan, revalidateSubscriptions]
  );

  const onPaymentIntentSucceeded = useCallback(
    (planType: SubscriptionPlan) => {
      revalidateSubscriptions();
      setIsUpgrading(false);
      toaster.positive(
        `Successfully upgraded to ${planType} plan. It might take a moment for the change to propagete through our systems.`,
        { autoHideDuration: 10000 }
      );

      // After 10 seconds webhook should surely be processed
      // TODO: find more elegant solution to this, e.g. websockets
      setTimeout(() => {
        revalidateSubscriptions();
        revalidateActivePlan();
      }, 10000);
    },
    [revalidateActivePlan, revalidateSubscriptions]
  );

  const onSubscriptionUpdated = useCallback(
    (subscription: SubscriptionDTO) => {
      updateSubscription(subscription);
      revalidateActivePlan();
    },
    [updateSubscription, revalidateActivePlan]
  );

  // TODO: this should be a separate SSR route
  if (selectedSubscription) {
    return (
      <SubscriptionDetailsContainer
        subscription={selectedSubscription}
        onSubscriptionUpdated={onSubscriptionUpdated}
      />
    );
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
          <CheckoutForm
            onPlanUpgraded={onPlanUpgraded}
            onPaymentIntentSucceeded={onPaymentIntentSucceeded}
          />
        </Modal>
      )}

      <Card
        title="Subscriptions"
        overrides={{ Root: { style: { marginTop: '20px' } } }}
      >
        <StyledBody>
          <SubscriptionList
            subscriptions={subscriptions}
            onClick={setSelectedSubscriptionId}
          />
        </StyledBody>
      </Card>
    </Block>
  );
};

export default React.memo(BillingOrganizationSettings);
