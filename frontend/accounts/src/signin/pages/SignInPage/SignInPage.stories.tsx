import React from 'react';
import type { Meta } from '@storybook/react';
import { appBaseUrl } from 'shared/config';

import { SignInPage } from './SignInPage';

export default {
  title: 'signin/pages/SignInPage',
  component: SignInPage,
} as Meta;

export const Base = () => {
  return <SignInPage redirect={appBaseUrl} />;
};
