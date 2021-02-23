import React from 'react';
import type { Meta } from '@storybook/react';
import { v4 as uuid } from 'uuid';

import { PasswordResetPage } from './PasswordResetPage';

export default {
  title: 'password/pages/PasswordResetPage',
  component: PasswordResetPage,
} as Meta;

export const Base = () => {
  return <PasswordResetPage token={uuid()} />;
};
