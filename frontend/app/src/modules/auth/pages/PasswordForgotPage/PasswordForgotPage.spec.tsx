import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@insight/testing';
import { waitFor } from '@testing-library/react';

import { Base } from './PasswordForgotPage.stories';

describe('<PasswordForgotPage />', () => {
  it('Should be able to start password reset flow', async () => {
    Base.story.setupMocks(sandbox);
    const { getByText, findByText, getByPlaceholderText } = render(<Base />);

    await userEvent.type(getByPlaceholderText('Email'), 'test-user@gmail.com');
    await userEvent.click(getByText('Reset password'));

    await findByText('Check your inbox!');
    await findByText(
      'If your email address is associated with an Insight account, you will be receiving a password reset request shortly.'
    );
  });

  it('Should be able to navigate to login if remember password', async () => {
    const { getByText, push } = render(<Base />);
    userEvent.click(getByText('Remember password?'));

    await waitFor(() => {
      sandbox.assert.calledWithExactly(push, '/login', '/login', {
        shallow: undefined,
      });
    });
  });
});
