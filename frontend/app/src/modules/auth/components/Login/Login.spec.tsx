import React from 'react';
import { render, clickElement, typeText, sandbox } from 'test/utils';
import { waitFor } from '@testing-library/react';

import { Base, InvalidPassword } from './Login.stories';

describe('<Login />', () => {
  it('Should validate input fields client side', async () => {
    const loginStub = Base.story.setupMocks(sandbox);
    const {
      getByPlaceholderText,
      getByText,
      findByText,
      findAllByText,
      replace,
    } = render(<Base />);
    const emailInput = getByPlaceholderText('Email');
    const passwordInput = getByPlaceholderText('Password');
    const submitButton = getByText('Sign in');

    clickElement(submitButton);
    expect((await findAllByText('Required')).length).toEqual(2);

    typeText(emailInput, 'invalid');
    typeText(passwordInput, 'short');

    clickElement(submitButton);
    await findByText('Please enter a valid email address');
    await findByText('Password must be at least 8 characters long');

    typeText(emailInput, 'user@example.com');
    typeText(passwordInput, 'veryHardPassword');

    clickElement(submitButton);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(
        loginStub,
        'user@example.com',
        'veryHardPassword'
      );
      sandbox.assert.calledWithExactly(replace, '/');
    });
  });

  it('Should display error on server side error', async () => {
    const loginStub = InvalidPassword.story.setupMocks(sandbox);
    const { getByPlaceholderText, getByText, findByText } = render(
      <InvalidPassword />
    );
    const emailInput = getByPlaceholderText('Email');
    const passwordInput = getByPlaceholderText('Password');
    const submitButton = getByText('Sign in');

    typeText(emailInput, 'user@example.com');
    typeText(passwordInput, 'veryHardPassword');
    clickElement(submitButton);

    await findByText('Invalid email or password');
    sandbox.assert.calledWithExactly(
      loginStub,
      'user@example.com',
      'veryHardPassword'
    );
  });

  it('Should render Google sign on button with correct link', () => {
    const { getByText } = render(<Base />);
    const signInWithGoogle = getByText('Sign in with Google');
    const googleAnchorElement = signInWithGoogle.parentElement as HTMLAnchorElement;
    expect(googleAnchorElement.href).toEqual(
      'http://localhost:8080/v1/sso/google/signin?dest=%2F'
    );
  });

  it('Should render Create a new account button with correct link', () => {
    const { getByText } = render(<Base />);
    const createFreeAccountButton = getByText('Create a free account');
    const freeAccountAnchor = createFreeAccountButton.parentElement as HTMLAnchorElement;
    expect(freeAccountAnchor.href).toEqual('http://localhost:3002/');
  });
});
