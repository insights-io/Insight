import React, { useState } from 'react';
import type { Meta } from '@storybook/react';

import { AutocompleteInput } from './AutocompleteInput';

export default {
  title: 'inputs/AutocompleteInput',
  component: AutocompleteInput,
} as Meta;

const useAutocompleteInput = () => {
  const [value, onChange] = useState('');
  return { value, onChange };
};

export const Base = () => {
  return (
    <AutocompleteInput
      {...useAutocompleteInput()}
      options={['Maribor', 'Ljubljana']}
      placeholder="Type something"
    />
  );
};
