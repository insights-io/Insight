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

export const WithStringOptions = () => {
  return (
    <AutocompleteInput
      {...useAutocompleteInput()}
      options={['Maribor', 'Ljubljana']}
      placeholder="Type something"
    />
  );
};

export const WithObjectOptions = () => {
  return (
    <AutocompleteInput
      {...useAutocompleteInput()}
      options={[
        { label: 'Maribor', value: 'maribor' },
        { label: 'Ljubljana', value: 'ljubljana' },
      ]}
      placeholder="Type something"
    />
  );
};
