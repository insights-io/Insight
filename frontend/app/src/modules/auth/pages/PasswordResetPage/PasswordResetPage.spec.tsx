import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@rebrowse/testing';
import { waitFor } from '@testing-library/react';

import { Base } from './PasswordResetPage.stories';

describe('<PasswordResetPage />', () => {
  it('Should navigate to / after password reset', async () => {
    const passwordResetStub = Base.story.setupMocks(sandbox);
    const { getByPlaceholderText, getByText, replace } = render(<Base />);
    await userEvent.type(getByPlaceholderText('Password'), 'password123');
    userEvent.click(getByText('Reset password and sign in'));

    await waitFor(() => {
      sandbox.assert.calledWithExactly(
        passwordResetStub,
        '1234',
        'password123'
      );
      sandbox.assert.calledWithExactly(replace, '/');
    });
  });
});
