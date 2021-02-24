import React from 'react';
import type { Meta } from '@storybook/react';

import { PasswordResetNotFoundPage } from './PasswordResetNotFoundPage';

export default {
  title: 'password/pages/PasswordResetNotFoundPage',
  component: PasswordResetNotFoundPage,
} as Meta;

export const Base = () => {
  return <PasswordResetNotFoundPage />;
};
