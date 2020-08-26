import React from 'react';
import CodeInput from 'shared/components/CodeInput';

import { TfaInputMethodProps } from '../types';

type Props = TfaInputMethodProps;

const TfaTotpInputMethod = ({ code, handleChange, error }: Props) => {
  return (
    <CodeInput
      label="Google verification code"
      code={code}
      handleChange={handleChange}
      error={error}
    />
  );
};

export default TfaTotpInputMethod;
