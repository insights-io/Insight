import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';

import { Base } from './Console.stories';

describe('<Console />', () => {
  it('Should display log and error events', async () => {
    const { queryByText, getByPlaceholderText } = render(<Base />);
    expect(queryByText('[Fast Refresh] done')).toBeInTheDocument();
    expect(queryByText('Uncaught Error: simulated error')).toBeInTheDocument();

    await userEvent.type(
      getByPlaceholderText('Filter'),
      'Unexpected identifier'
    );
    expect(queryByText('[Fast Refresh] done')).toBeNull();
    expect(queryByText('Uncaught Error: simulated error')).toBeNull();
    expect(
      queryByText('Uncaught SyntaxError: Unexpected identifier')
    ).toBeInTheDocument();
  });
});
