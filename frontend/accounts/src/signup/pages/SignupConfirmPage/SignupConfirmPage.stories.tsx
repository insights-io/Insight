import React from 'react';
import type { Meta } from '@storybook/react';

import { SignupConfirmPage } from './SignupConfirmPage';

export default {
  title: 'signup/pages/SignupConfirmPage',
  component: SignupConfirmPage,
} as Meta;

export const Base = () => {
  return <SignupConfirmPage />;
};
