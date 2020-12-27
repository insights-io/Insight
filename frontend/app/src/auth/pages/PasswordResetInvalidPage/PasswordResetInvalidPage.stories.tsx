import React from 'react';
import type { Meta } from '@storybook/react';

import { PasswordResetInvalidPage } from './PasswordResetInvalidPage';

export default {
  title: 'auth/pages/PasswordResetInvalidPage',
  component: PasswordResetInvalidPage,
} as Meta;

export const Base = () => {
  return <PasswordResetInvalidPage />;
};
