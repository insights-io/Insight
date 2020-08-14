import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';

import VerificationPage from './VerificationPage';

export default {
  title: 'auth/pages/VerificationPage',
  decorators: [fullHeightDecorator],
};

// https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M%7C0&cht=qr&chl=otpauth://totp/Insight:setup-tfa-full-flow@gmail.com?secret=V7M6W6KLVANOP2PY&issuer=Insight

export const Base = () => {
  return <VerificationPage />;
};
