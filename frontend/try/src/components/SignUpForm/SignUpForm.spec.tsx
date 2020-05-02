import React from 'react';
import { render } from 'test/utils';
import { waitFor } from '@testing-library/react';
import { clickElement, typeText, sandbox } from '@insight/testing';

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

    clickElement(submitButton);
    expect((await findAllByText('Required')).length).toEqual(5);

    typeText(firstNameInput, 'Joe');
    typeText(lastNameInput, 'Makarena');
    typeText(companyInput, 'Insight');
    typeText(emailInput, 'random');
    typeText(passwordInput, 'short');

    clickElement(submitButton);
    await findByText('Invalid email address');
    await findByText('Password must be at least 8 characters long');

    typeText(emailInput, 'user@example.com');
    typeText(passwordInput, 'veryHardPassword');

    clickElement(submitButton);

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
    typeText(phoneNumberInput, '51222333');

    clickElement(submitButton);
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
