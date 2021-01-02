import React from 'react';
import { render } from '__tests__/utils';

import { LoggedIn, NotLoggedIn } from './HomePage.stories';

describe('<HomePage />', () => {
  it('Should render link to go to app', () => {
    const { getByText } = render(<LoggedIn />);
    const navigateButton = getByText('Go to app');
    const link = navigateButton.parentNode as HTMLLinkElement;
    expect(link.href).toEqual('http://localhost:3000/');
  });

  it('Should render link to sign up', () => {
    const { getByText } = render(<NotLoggedIn />);
    const navigateButton = getByText('Get started');
    const link = navigateButton.parentNode as HTMLLinkElement;
    expect(link.href).toEqual('http://localhost:3002/');
  });
});
