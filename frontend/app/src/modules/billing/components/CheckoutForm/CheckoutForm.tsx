import React, { useState } from 'react';
import {
  CardElement,
  Elements,
  useElements,
  useStripe,
} from '@stripe/react-stripe-js';
import { loadStripe, StripeError } from '@stripe/stripe-js';
import { BillingApi } from 'api';
import { Card } from 'baseui/card';
import { Block } from 'baseui/block';
import { Button, SIZE, SHAPE } from 'baseui/button';
import FormError from 'shared/components/FormError';
import type { APIError, APIErrorDataResponse, PlanDTO } from '@insight/types';

import { confirmCardPayment, createCardPaymentMethod } from './stripe';

type Props = {
  onPlanUpgraded: (subscription: PlanDTO) => void;
};

const TEST_PUBLISHABLE_KEY =
  'pk_test_51HRYgqI1ysvdCIIxDuWTE0dKP7FxVsdDkDq1d7uEqF5u8kuVQLLFfk3KMCxliCt6qCpyLIkYzfiFAnVkK0GDBgJv005WZqBCHB';

const stripePromise = loadStripe(TEST_PUBLISHABLE_KEY);

export const CheckoutForm = (props: Props) => {
  return (
    <Elements stripe={stripePromise}>
      <StripeChekoutForm {...props} />
    </Elements>
  );
};

const StripeChekoutForm = ({ onPlanUpgraded }: Props) => {
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
            if (createSubscriptionResponse.plan) {
              onPlanUpgraded(createSubscriptionResponse.plan);

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
                  .then(onPlanUpgraded)
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
