import React from 'react';
import { render } from 'test/utils';
import { waitFor } from '@testing-library/react';
import { sandbox } from '@insight/testing';
import userEvent from '@testing-library/user-event';
import * as windowUtils from 'shared/utils/window';

import { Base, InvalidPassword, WithSsoRedirect } from './LoginPage.stories';

describe('<LoginPage />', () => {
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

    userEvent.click(submitButton);
    expect((await findAllByText('Required')).length).toEqual(2);

    await userEvent.type(emailInput, 'invalid');
    await userEvent.type(passwordInput, 'short');

    userEvent.click(submitButton);
    await findByText('Please enter a valid email address');
    await findByText('Password must be at least 8 characters long');

    userEvent.clear(emailInput);
    await userEvent.type(emailInput, 'user@example.com');
    userEvent.clear(passwordInput);
    await userEvent.type(passwordInput, 'veryHardPassword');

    userEvent.click(submitButton);

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

    await userEvent.type(emailInput, 'user@example.com');
    await userEvent.type(passwordInput, 'veryHardPassword');
    userEvent.click(submitButton);

    await findByText('Invalid email or password');
    sandbox.assert.calledWithExactly(
      loginStub,
      'user@example.com',
      'veryHardPassword'
    );
  });

  it('Should redirect to SSO provider when setup', async () => {
    const locationAsignStub = sandbox.stub(windowUtils, 'locationAssign');
    WithSsoRedirect.story.setupMocks(sandbox);
    const { getByText, getByPlaceholderText } = render(<WithSsoRedirect />);
    const emailInput = getByPlaceholderText('Email');
    const passwordInput = getByPlaceholderText('Password');
    const submitButton = getByText('Sign in');

    await userEvent.type(emailInput, 'user@example.com');
    await userEvent.type(passwordInput, 'veryHardPassword');

    userEvent.click(submitButton);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(
        locationAsignStub,
        'http://localhost:8080/v1/sso/saml/signin?redirect=%2Faccount%2Fsettings&email=7bd08ee1-baa8-421e-86d3-861508e28c31%40insight-io.com'
      );
    });
  });

  it('Should render Google sign on button with correct link', () => {
    const { getByText } = render(<Base />);
    const signInWithGoogle = getByText('Sign in with Google');
    const googleAnchorElement = signInWithGoogle.parentElement as HTMLAnchorElement;
    expect(googleAnchorElement.href).toEqual(
      'http://localhost:8080/v1/sso/oauth2/google/signin?redirect=%2F'
    );
  });

  it('Should render Github sign on button with correct link', () => {
    const { getByText } = render(<Base />);
    const signInWithGoogle = getByText('Sign in with Github');
    const googleAnchorElement = signInWithGoogle.parentElement as HTMLAnchorElement;
    expect(googleAnchorElement.href).toEqual(
      'http://localhost:8080/v1/sso/oauth2/github/signin?redirect=%2F'
    );
  });

  it('Should render Microsoft sign on button with correct link', () => {
    const { getByText } = render(<Base />);
    const signInWithGoogle = getByText('Sign in with Microsoft');
    const googleAnchorElement = signInWithGoogle.parentElement as HTMLAnchorElement;
    expect(googleAnchorElement.href).toEqual(
      'http://localhost:8080/v1/sso/oauth2/microsoft/signin?redirect=%2F'
    );
  });

  it('Should render Create a new account button with correct link', () => {
    const { getByText } = render(<Base />);
    const createFreeAccountButton = getByText('Create a free account');
    const freeAccountAnchor = createFreeAccountButton.parentElement as HTMLAnchorElement;
    expect(freeAccountAnchor.href).toEqual('http://localhost:3002/');
  });
});
