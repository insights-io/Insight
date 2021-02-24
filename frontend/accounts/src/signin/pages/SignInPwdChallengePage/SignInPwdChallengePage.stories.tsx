import React from 'react';
import type { Meta } from '@storybook/react';

import { SignInPwdChallengePage } from './SignInPwdChallengePage';

export default {
  title: 'signin/pages/SignInPwdChallengePage',
  component: SignInPwdChallengePage,
} as Meta;

export const Base = () => {
  return (
    <SignInPwdChallengePage
      email="john.doe@gmail.com"
      redirect="http://localhost:3000"
    />
  );
};
