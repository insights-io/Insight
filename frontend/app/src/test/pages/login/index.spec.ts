import { sandbox } from '@rebrowse/testing';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthApi } from 'api';
import { getPage } from 'next-page-tester';

describe('pages/login', () => {
  it('As a user I should be able to start password reset flow from login page', async () => {
    const passwordForgotStub = sandbox
      .stub(AuthApi.password, 'forgot')
      .resolves();

    const page = await getPage({ route: '/login' });
    render(page);

    userEvent.click(screen.getByText('Forgot?'));

    await screen.findByText('Remember password?');

    const email = 'user@gmail.com';
    await userEvent.type(screen.getByPlaceholderText('Email'), email);

    userEvent.click(screen.getByText('Reset password'));

    await screen.findByText('Check your inbox!');

    sandbox.assert.calledWithExactly(passwordForgotStub, email);
  });
});
