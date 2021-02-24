import React from 'react';
import type { Meta } from '@storybook/react';

import { CheckYourInboxPage } from './CheckYourInboxPage';

export default {
  title: 'password/pages/CheckYourInboxPage',
  component: CheckYourInboxPage,
} as Meta;

export const Base = () => {
  return <CheckYourInboxPage />;
};
