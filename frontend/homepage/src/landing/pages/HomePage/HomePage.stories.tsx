import React from 'react';
import type { Meta } from '@storybook/react';

import { HomePage } from './HomePage';

export default {
  title: 'home/pages/HomePage',
  component: HomePage,
} as Meta;

export const LoggedIn = () => {
  return <HomePage loggedIn />;
};

export const NotLoggedIn = () => {
  return <HomePage loggedIn={false} />;
};
