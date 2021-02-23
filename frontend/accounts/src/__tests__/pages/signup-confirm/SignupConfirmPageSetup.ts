import { screen, waitFor } from '@testing-library/react';
import { SIGNUP_CONFIRM_ROUTE } from 'shared/constants/routes';
import { getPage } from '__tests__/utils';

export const MATCHERS = {
  heading: ['heading', { name: 'Confirm your email address!' }],
  subheading: [
    'heading',
    {
      name:
        'We have sent an email with a confirmation link to your email address.',
    },
  ],
} as const;

export const setup = async (route = SIGNUP_CONFIRM_ROUTE) => {
  const { render } = await getPage({ route });
  render();
  return getElements();
};

export const findElements = () => {
  return waitFor(() => getElements());
};

export const getElements = () => {
  return {
    heading: screen.getByRole(...MATCHERS.heading),
    subheading: screen.getByRole(...MATCHERS.subheading),
  };
};
