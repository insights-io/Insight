import React from 'react';
import { CodeInput } from '@insight/elements';

import { TfaInputMethodProps } from '../types';

type Props = TfaInputMethodProps;

export const TfaTotpInputMethod = ({ code, handleChange, error }: Props) => {
  return (
    <CodeInput
      label="Google verification code"
      code={code}
      handleChange={handleChange}
      error={error}
    />
  );
};
