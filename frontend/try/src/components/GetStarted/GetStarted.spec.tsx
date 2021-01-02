import React from 'react';
import { render } from '__tests__/utils';
import { appBaseURL, helpBaseURL } from 'shared/config';

import { Base } from './GetStarted.stories';

describe('<GetStarted />', () => {
  test('Contains all relevant data', () => {
    const { getByText } = render(<Base />);
    expect(getByText('Rebrowse')).toBeInTheDocument();
    expect(getByText('Start your free trial now.')).toBeInTheDocument();
    expect(getByText("You're minutes away from insights.")).toBeInTheDocument();

    const helpButton = getByText('Help');
    expect(helpButton.parentElement?.getAttribute('href')).toEqual(helpBaseURL);

    const logInButton = getByText('Log in');
    expect(logInButton.parentElement?.getAttribute('href')).toEqual(appBaseURL);
  });
});
