import { sandbox } from '@rebrowse/testing';
import { waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthApi } from 'api';
import React from 'react';
import { render } from 'test/utils';

import LoginEmailForm from './LoginEmailForm';

describe('<LoginEmailForm />', () => {
  it('Should redirect to verification on negative data response', async () => {
    const loginStub = sandbox
      .stub(AuthApi.sso.session, 'login')
      .resolves({ data: false });

    const replaceStub = sandbox.stub();
    const { getByPlaceholderText, getByText } = render(
      <LoginEmailForm replace={replaceStub} relativeRedirect="/" />
    );

    const email = 'user@gmail.com';
    const password = 'superPassword';
    await userEvent.type(getByPlaceholderText('Email'), email);
    await userEvent.type(getByPlaceholderText('Password'), password);

    userEvent.click(getByText('Sign in'));

    await waitFor(() => {
      sandbox.assert.calledWithExactly(loginStub, email, password);
      sandbox.assert.calledWithExactly(
        replaceStub,
        '/login/verification?redirect=%2F'
      );
    });
  });
});
