import React, { useCallback, useMemo, useState } from 'react';
import { Block } from 'baseui/block';
import { YourPlan } from 'billing/components/YourPlan';
import { addDays } from 'date-fns';
import { Modal } from 'baseui/modal';
import { CheckoutForm } from 'billing/components/CheckoutForm';
import { SubscriptionList } from 'billing/components/SubscriptionList';
import { toaster } from 'baseui/toast';
import type {
  Organization,
  PlanDTO,
  Subscription,
  SubscriptionPlan,
} from '@rebrowse/types';
import { Panel } from '@rebrowse/elements';

type Props = {
  subscriptions: Subscription[];
  organization: Organization;
  plan: PlanDTO;
  refetchSubscriptions: () => void;
  refetchActivePlan: () => void;
  setActivePlan: (plan: PlanDTO) => void;
};

export const BillingSubscription = ({
  organization,
  plan,
  subscriptions,
  refetchSubscriptions,
  refetchActivePlan,
  setActivePlan,
}: Props) => {
  const [isUpgrading, setIsUpgrading] = useState(false);
  const onUpgradeClick = useCallback(() => setIsUpgrading(true), []);
  const resetsOn = useMemo(() => addDays(organization.createdAt, 30), [
    organization.createdAt,
  ]);

  const onPaymentIntentSucceeded = useCallback(
    (planType: SubscriptionPlan) => {
      refetchSubscriptions();
      setIsUpgrading(false);
      toaster.positive(
        `Successfully upgraded to ${planType} plan. It might take a moment for the change to propagete through our systems.`,
        { autoHideDuration: 10000 }
      );

      // After 10 seconds webhook should surely be processed
      // TODO: find more elegant solution to this, e.g. websockets
      setTimeout(() => {
        refetchSubscriptions();
        refetchActivePlan();
      }, 10000);
    },
    [refetchActivePlan, refetchSubscriptions]
  );

  const onPlanUpgraded = useCallback(
    (upgradedPlan: PlanDTO) => {
      refetchSubscriptions();
      setActivePlan(upgradedPlan);
      setIsUpgrading(false);
      toaster.positive(`Successfully upgraded to ${upgradedPlan.type} plan`, {
        autoHideDuration: 10000,
      });
    },
    [setActivePlan, refetchSubscriptions]
  );

  return (
    <Block>
      <YourPlan
        sessionsUsed={0}
        plan={plan.type}
        dataRetention={plan.dataRetention}
        resetsOn={resetsOn}
        onUpgradeClick={onUpgradeClick}
        isLoading={false}
      />

      {plan?.type !== 'enterprise' && (
        <Modal isOpen={isUpgrading} onClose={() => setIsUpgrading(false)}>
          <CheckoutForm
            onPlanUpgraded={onPlanUpgraded}
            onPaymentIntentSucceeded={onPaymentIntentSucceeded}
          />
        </Modal>
      )}

      {subscriptions.length > 0 && (
        <Panel $style={{ marginTop: '32px' }}>
          <Panel.Header>Subscriptions</Panel.Header>
          <Panel.Item>
            <SubscriptionList subscriptions={subscriptions} />
          </Panel.Item>
        </Panel>
      )}
    </Block>
  );
};
