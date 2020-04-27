import React from 'react';
import { render } from 'test/utils';
import config from 'shared/config';

import { Base } from './GetStarted.stories';

describe('<GetStarted />', () => {
  test('Contains all relevant data', () => {
    const { getByText } = render(<Base />);
    expect(getByText('Insight')).toBeInTheDocument();
    expect(getByText('Start your free trial now.')).toBeInTheDocument();
    expect(getByText("You're minutes away from insights.")).toBeInTheDocument();

    const helpButton = getByText('Help');
    expect(helpButton.parentElement?.getAttribute('href')).toEqual(
      config.helpBaseURL
    );

    const logInButton = getByText('Log in');
    expect(logInButton.parentElement?.getAttribute('href')).toEqual(
      config.appBaseURL
    );
  });
});
