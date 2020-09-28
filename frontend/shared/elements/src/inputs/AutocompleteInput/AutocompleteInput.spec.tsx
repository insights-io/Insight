import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';

import { Base } from './AutocompleteInput.stories';

describe('<AutocompleteInput />', () => {
  it('Should work as expected', async () => {
    const { container, queryByText } = render(<Base />);
    const autocompleteInput = container.querySelector(
      'input'
    ) as HTMLInputElement;

    expect(queryByText('Type something')).toBeInTheDocument();
    expect(queryByText('Maribor')).toBeNull();
    expect(queryByText('Ljubljana')).toBeNull();

    userEvent.click(autocompleteInput);
    expect(queryByText('Maribor')).toBeInTheDocument();
    expect(queryByText('Ljubljana')).toBeInTheDocument();

    await userEvent.type(autocompleteInput, 'ma');
    expect(queryByText('Maribor')).toBeInTheDocument();
    expect(queryByText('Ljubljana')).toBeNull();
    expect(queryByText('Type something')).toBeNull();
  });
});
