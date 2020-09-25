import React, { useMemo, useState, useCallback } from 'react';
import { Card, StyledBody } from 'baseui/card';
import { Block } from 'baseui/block';
import YourPlan from 'modules/billing/components/YourPlan';
import { Modal } from 'baseui/modal';
import { addDays } from 'date-fns';
import useActivePlan from 'modules/billing/hooks/useActivePlan';
import type { PlanDTO } from '@insight/types';
import useSubscriptions from 'modules/billing/hooks/useSubscriptions';
import { SubscriptionList } from 'modules/billing/components/SubscriptionList';
import { CheckoutForm } from 'modules/billing/components/CheckoutForm';

type Props = {
  organizationCreatedAt: Date | undefined;
};

const BillingOrganizationSettings = ({ organizationCreatedAt }: Props) => {
  const [isUpgrading, setIsUpgrading] = useState(false);
  const { subscriptions } = useSubscriptions();
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
      setActivePlan(upgradedPlan);
      setIsUpgrading(false);
    },
    [setActivePlan]
  );

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
          <SubscriptionList subscriptions={subscriptions} />
        </StyledBody>
      </Card>
    </Block>
  );
};

export default React.memo(BillingOrganizationSettings);
