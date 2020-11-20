import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@rebrowse/testing';
import { waitFor } from '@testing-library/react';

import { Base } from './PasswordResetInvalidPage.stories';

describe('<PasswordResetInvalidPage />', () => {
  it('Should be able to navigate back to /login', async () => {
    const { queryByText, getByText, push } = render(<Base />);
    expect(
      queryByText(
        'It looks like this password reset request is invalid or has already been accepted.'
      )
    ).toBeInTheDocument();
    userEvent.click(getByText('Log in or reset your password'));

    await waitFor(() => {
      sandbox.assert.calledWithExactly(push, '/login', '/login', {
        shallow: undefined,
        locale: undefined,
      });
    });
  });
});
