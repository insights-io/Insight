import React from 'react';
import { CodeInput } from '@rebrowse/elements';
import type { MfaInputProps } from 'signin/types';

type Props = MfaInputProps;

export const TotpMfaInput = ({ code, handleChange, error }: Props) => {
  return (
    <CodeInput
      label="Google verification code"
      code={code}
      handleChange={handleChange}
      error={error}
    />
  );
};
