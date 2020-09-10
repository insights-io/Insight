import React from 'react';
import { render } from 'test/utils';

import { LoggedIn, NotLoggedIn } from './HomePage.stories';

describe('<HomePage />', () => {
  it('Should render link to go to app', () => {
    const { getByText } = render(<LoggedIn />);
    const navigateButton = getByText('Go to app');
    const link = navigateButton.parentNode as HTMLLinkElement;
    expect(link.href).toEqual('http://localhost:3002/');
    expect(link.target).toEqual('_blank');
    expect(link.rel).toEqual('noreferrer noopener');
  });

  it('Should render link to sign up', () => {
    const { getByText } = render(<NotLoggedIn />);
    const navigateButton = getByText('Sign up');
    const link = navigateButton.parentNode as HTMLLinkElement;
    expect(link.href).toEqual('http://localhost:3000/');
    expect(link.target).toEqual('_blank');
    expect(link.rel).toEqual('noreferrer noopener');
  });
});
