import React from 'react';
import type { Meta } from '@storybook/react';

import { PasswordInput, PasswordInputProps } from './PasswordInput';

export default {
  title: 'inputs/PasswordInput',
  component: PasswordInput,
} as Meta;

export const Base = (props?: PasswordInputProps) => {
  return <PasswordInput {...props} />;
};
