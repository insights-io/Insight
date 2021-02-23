import React from 'react';
import type { Meta } from '@storybook/react';

import { Divider } from './Divider';

export default {
  title: 'atoms/Divider',
  component: Divider,
} as Meta;

export const Base = () => {
  return (
    <Divider>
      <Divider.Line />
    </Divider>
  );
};

export const Or = () => {
  return (
    <Divider>
      <Divider.Line />
      <Divider.Or />
      <Divider.Line />
    </Divider>
  );
};
