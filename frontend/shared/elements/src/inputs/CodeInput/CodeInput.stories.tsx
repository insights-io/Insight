import React from 'react';
import type { Meta } from '@storybook/react';
import { useCodeInput } from 'hooks';
import { action } from '@storybook/addon-actions';

import { CodeInput } from './CodeInput';

export default {
  title: 'inputs/CodeInput',
  component: CodeInput,
} as Meta;

const useCodeInputStory = () => {
  return useCodeInput({
    handleError: action('handleError'),
    submitAction: async () => action('submitAction')(),
  });
};

export const Base = () => {
  return <CodeInput {...useCodeInputStory()} />;
};

export const WithLabel = () => {
  return <CodeInput {...useCodeInputStory()} label="SMS code" />;
};

export const WithError = () => {
  return (
    <CodeInput
      {...useCodeInputStory()}
      label="SMS code"
      error="Something went wrong"
    />
  );
};
