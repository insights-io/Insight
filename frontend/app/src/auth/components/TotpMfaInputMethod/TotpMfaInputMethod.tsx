import React from 'react';
import { CodeInput } from '@rebrowse/elements';

import { MfaInputMethodProps } from '../types';

type Props = MfaInputMethodProps;

export const TotpMfaInputMethod = ({ code, handleChange, error }: Props) => {
  return (
    <CodeInput
      label="Google verification code"
      code={code}
      handleChange={handleChange}
      error={error}
    />
  );
};
