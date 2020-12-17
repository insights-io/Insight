import { screen } from '@testing-library/react';
import React from 'react';
import { render } from 'test/utils';

import { Base } from './InsightPage.stories';

describe('<InsightsPage />', () => {
  it('Should render charts', () => {
    render(<Base />);

    expect(screen.getByText('Country distribution')).toBeInTheDocument();
    expect(screen.getByText('Continent distribution')).toBeInTheDocument();
    expect(screen.getByText('Device distribution')).toBeInTheDocument();
    expect(screen.getByText('Page Visits')).toBeInTheDocument();
    expect(screen.getByText('Sessions')).toBeInTheDocument();

    expect(screen.getAllByText(2184).length).toBe(2);
  });
});
