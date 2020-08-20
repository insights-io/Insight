import React, { useState } from 'react';

import AutocompleteInput from './AutocompleteInput';

export default {
  title: 'shared/components/AutocompleteInput',
};

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
