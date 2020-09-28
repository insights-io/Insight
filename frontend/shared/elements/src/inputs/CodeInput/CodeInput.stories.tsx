import React, { useState } from 'react';
import type { Meta } from '@storybook/react';

import { CodeInput } from './CodeInput';

export default {
  title: 'inputs/CodeInput',
  component: CodeInput,
} as Meta;

const useCodeInput = () => {
  const [code, handleChange] = useState(['', '', '', '']);
  return {
    code,
    handleChange,
    error: undefined,
    label: 'Label',
  };
};

export const FourCharacters = () => {
  return <CodeInput {...useCodeInput()} />;
};
