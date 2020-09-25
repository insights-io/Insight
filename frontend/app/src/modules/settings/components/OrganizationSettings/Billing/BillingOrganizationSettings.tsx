import React, { useMemo, useState, useCallback } from 'react';
import { loadStripe, StripeError } from '@stripe/stripe-js';
import {
  CardElement,
  Elements,
  useStripe,
  useElements,
} from '@stripe/react-stripe-js';
import { Button, SHAPE, SIZE } from 'baseui/button';
import { Card, StyledBody } from 'baseui/card';
import { Block } from 'baseui/block';
import { BillingApi } from 'api';
import { toaster } from 'baseui/toast';
import FormError from 'shared/components/FormError';
import YourPlan from 'modules/billing/components/YourPlan';
import { Modal } from 'baseui/modal';
import { addDays } from 'date-fns';
import useActivePlan from 'modules/billing/hooks/useActivePlan';
import type { APIError, APIErrorDataResponse, PlanDTO } from '@insight/types';
import useSubscriptions from 'modules/billing/hooks/useSubscriptions';
import { SubscriptionList } from 'modules/billing/components/SubscriptionList';

import { createCardPaymentMethod, confirmCardPayment } from './stripe';

const TEST_PUBLISHABLE_KEY =
  'pk_test_51HRYgqI1ysvdCIIxDuWTE0dKP7FxVsdDkDq1d7uEqF5u8kuVQLLFfk3KMCxliCt6qCpyLIkYzfiFAnVkK0GDBgJv005WZqBCHB';

const stripePromise = loadStripe(TEST_PUBLISHABLE_KEY);

type CheckoutFormProps = {
  onPlanUpgraded: (plan: PlanDTO) => void;
};

const CheckoutForm = ({ onPlanUpgraded }: CheckoutFormProps) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [stripeSetupError, setStripeSetupError] = useState<StripeError>();
  const [apiError, setApiError] = useState<APIError>();
  const stripe = useStripe();
  const elements = useElements();

  const handlePlanUpdated = (plan: PlanDTO) => {
    onPlanUpgraded(plan);
    toaster.positive(`Successfully upgraded plan to ${plan.type}`, {});
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!stripe || !elements || isSubmitting) {
      return;
    }

    setApiError(undefined);
    setStripeSetupError(undefined);
    setIsSubmitting(true);

    try {
      const { error, paymentMethod } = await createCardPaymentMethod(
        stripe,
        elements
      );

      if (error) {
        setStripeSetupError(error);
      } else if (paymentMethod) {
        await BillingApi.subscriptions
          .create({ paymentMethodId: paymentMethod.id, plan: 'business' })
          .then((createSubscriptionResponse) => {
            if (createSubscriptionResponse.plan) {
              handlePlanUpdated(createSubscriptionResponse.plan);

              return Promise.resolve();
            }

            return confirmCardPayment(
              createSubscriptionResponse.clientSecret,
              stripe,
              elements
            ).then((confirmation) => {
              if (confirmation.error) {
                setStripeSetupError(confirmation.error);
              } else if (confirmation.paymentIntent?.status === 'succeeded') {
                BillingApi.subscriptions
                  .getActivePlan()
                  .then(handlePlanUpdated)
                  .catch(async (apiErrorResponse) => {
                    const errorDTO: APIErrorDataResponse = await apiErrorResponse.response.json();
                    setApiError(errorDTO.error);
                  });
              }
            });
          })
          .catch(async (apiErrorResponse) => {
            const errorDTO: APIErrorDataResponse = await apiErrorResponse.response.json();
            setApiError(errorDTO.error);
          });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Card>
      <form onSubmit={handleSubmit}>
        <CardElement options={{ iconStyle: 'solid', hidePostalCode: true }} />
        <Block marginTop="24px">
          <Button
            type="submit"
            disabled={!stripe}
            size={SIZE.compact}
            shape={SHAPE.pill}
            isLoading={isSubmitting}
            $style={{ width: '100%' }}
          >
            Pay
          </Button>
        </Block>
        {apiError && <FormError error={apiError} />}
        {stripeSetupError && (
          <FormError
            error={{
              message: stripeSetupError.message || stripeSetupError.type,
            }}
          />
        )}
      </form>
    </Card>
  );
};

type Props = {
  organizationCreatedAt: Date | undefined;
};

const BillingOrganizationSettings = ({ organizationCreatedAt }: Props) => {
  const [isUpgrading, setIsUpgrading] = useState(false);
  const {
    plan,
    isLoading: isLoadingActivePlan,
    setActivePlan,
  } = useActivePlan();

  const { subscriptions } = useSubscriptions();

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
        plan={plan?.type}
        sessionsUsed={0}
        dataRetention={plan?.dataRetention}
        resetsOn={resetsOn}
        onUpgradeClick={onUpgradeClick}
        isLoading={isLoadingActivePlan}
      />
      {plan?.type !== 'enterprise' && (
        <Modal isOpen={isUpgrading} onClose={() => setIsUpgrading(false)}>
          <Elements stripe={stripePromise}>
            <CheckoutForm onPlanUpgraded={onPlanUpgraded} />
          </Elements>
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
