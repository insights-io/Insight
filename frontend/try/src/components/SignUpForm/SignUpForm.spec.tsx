import React from 'react';
import { render } from '__tests__/utils';
import { waitFor } from '@testing-library/react';
import { sandbox } from '@rebrowse/testing';
import userEvent from '@testing-library/user-event';

import { Base } from './SignUpForm.stories';

describe('<SignUpForm />', () => {
  test('User can signup in normal flow', async () => {
    const onSubmit = sandbox.stub().resolves(Promise.resolve());

    const {
      getByPlaceholderText,
      getByText,
      findByText,
      findAllByText,
    } = render(<Base onSubmit={onSubmit} />);

    const submitButton = getByText('Get started');
    const firstNameInput = getByPlaceholderText('John Doe');
    const companyInput = getByPlaceholderText('Example');
    const emailInput = getByPlaceholderText('john.doe@gmail.com');
    const passwordInput = getByPlaceholderText('Password');
    const phoneNumberInput = getByPlaceholderText('51111222');

    userEvent.click(submitButton);
    expect((await findAllByText('Required')).length).toEqual(4);

    await userEvent.type(firstNameInput, 'Joe Makarena');
    await userEvent.type(companyInput, 'Rebrowse');
    await userEvent.type(emailInput, 'random');
    await userEvent.type(passwordInput, 'short');

    userEvent.click(submitButton);
    await findByText('Invalid email address');
    await findByText('Password must be at least 8 characters long');

    userEvent.clear(emailInput);
    await userEvent.type(emailInput, 'user@example.com');
    userEvent.clear(passwordInput);
    await userEvent.type(passwordInput, 'veryHardPassword');

    userEvent.click(submitButton);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(onSubmit, {
        fullName: 'Joe Makarena',
        company: 'Rebrowse',
        email: 'user@example.com',
        password: 'veryHardPassword',
        phoneNumber: undefined,
      });
    });

    // can also include phone number
    const phoneNumber = '51111222';
    await userEvent.type(phoneNumberInput, phoneNumber);

    userEvent.click(submitButton);
    await waitFor(() => {
      sandbox.assert.calledWithExactly(onSubmit, {
        fullName: 'Joe Makarena',
        company: 'Rebrowse',
        email: 'user@example.com',
        password: 'veryHardPassword',
        phoneNumber: { countryCode: '+1', digits: phoneNumber },
      });
    });
  });
});
