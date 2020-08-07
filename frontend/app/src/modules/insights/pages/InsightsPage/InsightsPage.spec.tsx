import React from 'react';
import { render } from 'test/utils';

import { Base } from './InsightPage.stories';

describe('<InsightsPage />', () => {
  it('Should render charts', () => {
    const { queryByText } = render(<Base />);

    expect(queryByText('Location distribution')).toBeInTheDocument();
    expect(queryByText('Device distribution')).toBeInTheDocument();
  });
});
