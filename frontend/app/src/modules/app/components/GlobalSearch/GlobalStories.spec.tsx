import React from 'react';
import { render, blurElement, focusElement } from 'test/utils';
import { waitFor } from '@testing-library/react';

import { Base } from './GlobalSearch.stories';

describe('<GlobalSearch />', () => {
  it('Can open & close GlobalSearch', async () => {
    const { getByPlaceholderText, findAllByText, queryAllByText } = render(
      <Base />
    );
    const searchInput = getByPlaceholderText('Search insights...');
    expect(queryAllByText('item').length).toEqual(0);

    /* Open via focus */
    focusElement(searchInput);
    expect((await findAllByText('item')).length).toEqual(5);

    /* Close via blur */
    blurElement(searchInput);
    await waitFor(() => {
      expect(queryAllByText('item').length).toEqual(0);
    });
  });
});
