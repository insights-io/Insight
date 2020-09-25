import { Stripe, StripeCardElement, StripeElements } from '@stripe/stripe-js';
import { CardElement } from '@stripe/react-stripe-js';

export const createCardPaymentMethod = (
  stripe: Stripe,
  elements: StripeElements
) => {
  return stripe.createPaymentMethod({
    type: 'card',
    card: elements.getElement(CardElement) as StripeCardElement,
  });
};

export const confirmCardPayment = (
  clientSecret: string,
  stripe: Stripe,
  elements: StripeElements
) => {
  return stripe.confirmCardPayment(clientSecret, {
    payment_method: {
      card: elements.getElement(CardElement) as StripeCardElement,
    },
  });
};
