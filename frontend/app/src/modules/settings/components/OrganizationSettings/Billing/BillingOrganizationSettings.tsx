import React, { useState } from 'react';
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
import { APIError, APIErrorDataResponse } from '@insight/types';
import FormError from 'shared/components/FormError';

import { createCardPaymentMethod } from './stripe';

const TEST_PUBLISHABLE_KEY =
  'pk_test_51HRYgqI1ysvdCIIxDuWTE0dKP7FxVsdDkDq1d7uEqF5u8kuVQLLFfk3KMCxliCt6qCpyLIkYzfiFAnVkK0GDBgJv005WZqBCHB';

const stripePromise = loadStripe(TEST_PUBLISHABLE_KEY);

const CheckoutForm = () => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [_stripeSetupError, setStripeSetupError] = useState<StripeError>();
  const [apiError, setApiError] = useState<APIError>();
  const stripe = useStripe();
  const elements = useElements();

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!stripe || !elements || isSubmitting) {
      return;
    }

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
          .create({ paymentMethodId: paymentMethod.id })
          .then(() => toaster.positive('Subscription created.', {}))
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
      </form>
    </Card>
  );
};

const BillingOrganizationSettings = () => {
  return (
    <Elements stripe={stripePromise}>
      <CheckoutForm />
    </Elements>
  );
};

export default React.memo(BillingOrganizationSettings);
