import React from 'react';
import { render } from 'test/utils';
import { waitFor, fireEvent } from '@testing-library/react';

import { Base } from './GlobalSearch.stories';

describe('<GlobalSearch />', () => {
  it('Can open & close GlobalSearch', async () => {
    const { getByPlaceholderText, findAllByText, queryAllByText } = render(
      <Base />
    );
    const searchInput = getByPlaceholderText('Search insights...');
    expect(queryAllByText('item').length).toEqual(0);

    /* Open via focus */
    fireEvent.focus(searchInput);
    expect((await findAllByText('item')).length).toEqual(5);

    /* Close via blur */
    fireEvent.blur(searchInput);
    await waitFor(() => {
      expect(queryAllByText('item').length).toEqual(0);
    });
  });
});
