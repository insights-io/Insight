import React from 'react';
import type { Meta } from '@storybook/react';

import { SignUpConfirmPage } from './SignUpConfirmPage';

export default {
  title: 'signup/pages/SignUpConfirmPage',
  component: SignUpConfirmPage,
} as Meta;

export const Base = () => {
  return <SignUpConfirmPage />;
};
