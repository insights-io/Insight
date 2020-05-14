import React from 'react';
import { render } from 'test/utils';
import { waitFor } from '@testing-library/react';
import { sandbox } from '@insight/testing';
import userEvent from '@testing-library/user-event';

import { Base } from './SignUpForm.stories';

describe('<SignUpForm />', () => {
  test('User can signup in normal flow', async () => {
    const onSubmit = sandbox.stub().resolves(undefined);

    const {
      getByPlaceholderText,
      getByText,
      findByText,
      findAllByText,
    } = render(<Base onSubmit={onSubmit} />);
    const submitButton = getByText('Get started');
    const firstNameInput = getByPlaceholderText('First name');
    const lastNameInput = getByPlaceholderText('Last name');
    const companyInput = getByPlaceholderText('Company');
    const emailInput = getByPlaceholderText('Email');
    const passwordInput = getByPlaceholderText('Password');

    userEvent.click(submitButton);
    expect((await findAllByText('Required')).length).toEqual(5);

    userEvent.type(firstNameInput, 'Joe');
    userEvent.type(lastNameInput, 'Makarena');
    userEvent.type(companyInput, 'Insight');
    userEvent.type(emailInput, 'random');
    userEvent.type(passwordInput, 'short');

    userEvent.click(submitButton);
    await findByText('Invalid email address');
    await findByText('Password must be at least 8 characters long');

    userEvent.clear(emailInput);
    userEvent.type(emailInput, 'user@example.com');
    userEvent.clear(passwordInput);
    userEvent.type(passwordInput, 'veryHardPassword');

    userEvent.click(submitButton);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(onSubmit, {
        firstName: 'Joe',
        lastName: 'Makarena',
        company: 'Insight',
        email: 'user@example.com',
        password: 'veryHardPassword',
      });
    });

    // can also include phone nume
    const phoneNumberInput = getByPlaceholderText('Phone number');
    userEvent.type(phoneNumberInput, '51222333');

    userEvent.click(submitButton);
    await waitFor(() => {
      sandbox.assert.calledWithExactly(onSubmit, {
        firstName: 'Joe',
        lastName: 'Makarena',
        company: 'Insight',
        email: 'user@example.com',
        password: 'veryHardPassword',
        phoneNumber: '+151222333',
      });
    });
  });
});
