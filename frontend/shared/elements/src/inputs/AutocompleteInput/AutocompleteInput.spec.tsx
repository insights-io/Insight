import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';

import {
  WithStringOptions,
  WithObjectOptions,
} from './AutocompleteInput.stories';

describe('<AutocompleteInput />', () => {
  [
    { name: 'strings', component: WithStringOptions },
    { name: 'objects', component: WithObjectOptions },
  ].forEach((fixture) => {
    it(`Should work as expected with ${fixture.name} options`, async () => {
      const { container, queryByText } = render(<fixture.component />);
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
});
