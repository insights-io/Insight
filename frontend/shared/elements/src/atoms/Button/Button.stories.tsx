import React from 'react';
import type { Meta } from '@storybook/react';

import { Button } from './Button';

export default {
  title: 'atoms/Button',
  component: Button,
} as Meta;

export const Base = () => {
  return <Button>Continue</Button>;
};
