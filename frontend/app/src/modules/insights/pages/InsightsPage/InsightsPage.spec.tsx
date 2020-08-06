import React from 'react';
import { render } from 'test/utils';

import { Base } from './InsightPage.stories';

describe('<InsightsPage />', () => {
  it('Should render charts', () => {
    const { queryByText } = render(<Base />);

    expect(queryByText('By country')).toBeInTheDocument();
    expect(queryByText('By device')).toBeInTheDocument();
  });
});
