import React from 'react';
import { render } from 'test/utils';
import { waitFor } from '@testing-library/react';
import { sandbox } from '@insight/testing';
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
    const firstNameInput = getByPlaceholderText('Full name');
    const companyInput = getByPlaceholderText('Company');
    const emailInput = getByPlaceholderText('Email');
    const passwordInput = getByPlaceholderText('Password');

    userEvent.click(submitButton);
    expect((await findAllByText('Required')).length).toEqual(4);

    await userEvent.type(firstNameInput, 'Joe Makarena');
    await userEvent.type(companyInput, 'Insight');
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
        company: 'Insight',
        email: 'user@example.com',
        password: 'veryHardPassword',
      });
    });

    // can also include phone nume
    const phoneNumberInput = getByPlaceholderText('Phone number');
    await userEvent.type(phoneNumberInput, '51222333');

    userEvent.click(submitButton);
    await waitFor(() => {
      sandbox.assert.calledWithExactly(onSubmit, {
        fullName: 'Joe Makarena',
        company: 'Insight',
        email: 'user@example.com',
        password: 'veryHardPassword',
        phoneNumber: '+151222333',
      });
    });
  });
});
