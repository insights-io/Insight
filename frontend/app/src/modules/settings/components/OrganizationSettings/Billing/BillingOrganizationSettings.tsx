import React, { useMemo, useState, useCallback } from 'react';
import { loadStripe, StripeError } from '@stripe/stripe-js';
import {
  CardElement,
  Elements,
  useStripe,
  useElements,
} from '@stripe/react-stripe-js';
import { Button, SHAPE, SIZE } from 'baseui/button';
import { Card } from 'baseui/card';
import { Block } from 'baseui/block';
import { BillingApi } from 'api';
import { toaster } from 'baseui/toast';
import {
  APIError,
  APIErrorDataResponse,
  SubscriptionDTO,
} from '@insight/types';
import FormError from 'shared/components/FormError';
import YourPlan from 'modules/billing/components/YourPlan';
import { Modal } from 'baseui/modal';
import { addDays } from 'date-fns';
import useSubscription from 'modules/billing/hooks/useSubscription';

import { createCardPaymentMethod, confirmCardPayment } from './stripe';

const TEST_PUBLISHABLE_KEY =
  'pk_test_51HRYgqI1ysvdCIIxDuWTE0dKP7FxVsdDkDq1d7uEqF5u8kuVQLLFfk3KMCxliCt6qCpyLIkYzfiFAnVkK0GDBgJv005WZqBCHB';

const stripePromise = loadStripe(TEST_PUBLISHABLE_KEY);

type CheckoutFormProps = {
  onSubscriptionCreated: (subscription: SubscriptionDTO) => void;
};

const CheckoutForm = ({ onSubscriptionCreated }: CheckoutFormProps) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [stripeSetupError, setStripeSetupError] = useState<StripeError>();
  const [apiError, setApiError] = useState<APIError>();
  const stripe = useStripe();
  const elements = useElements();

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
            if (createSubscriptionResponse.subscription) {
              onSubscriptionCreated(createSubscriptionResponse.subscription);
              toaster.positive('Subscription created', {});
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
                  .get()
                  .then((subscription) => {
                    onSubscriptionCreated(subscription);
                    toaster.positive('Subscription created', {});
                  })
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
  const { subscription, isLoading, setSubscription } = useSubscription();

  const onUpgradeClick = useCallback(() => setIsUpgrading(true), []);

  const resetsOn = useMemo(
    () =>
      organizationCreatedAt ? addDays(organizationCreatedAt, 30) : undefined,
    [organizationCreatedAt]
  );

  const onSubscriptionCreated = useCallback(
    (createdSubscription: SubscriptionDTO) => {
      setSubscription(createdSubscription);
      setIsUpgrading(false);
    },
    [setSubscription]
  );

  return subscription ? (
    <>
      <YourPlan
        plan={subscription.plan}
        sessionsUsed={0}
        dataRetention="1mo"
        resetsOn={resetsOn}
        onUpgradeClick={onUpgradeClick}
        isLoading={isLoading}
      />
      {subscription.plan !== 'enterprise' && (
        <Modal isOpen={isUpgrading} onClose={() => setIsUpgrading(false)}>
          <Elements stripe={stripePromise}>
            <CheckoutForm onSubscriptionCreated={onSubscriptionCreated} />
          </Elements>
        </Modal>
      )}
    </>
  ) : null;
};

export default React.memo(BillingOrganizationSettings);
