import { render, screen } from '@testing-library/react';
import { getPage } from 'next-page-tester';
import { appBaseURL, helpBaseURL } from 'shared/config';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@rebrowse/testing';
import { sdk } from 'api';

describe('/', () => {
  /* Data */
  const fullName = 'Joe Makarena';
  const company = 'Rebrowse';
  const email = 'user@example.com';
  const password = 'veryHardPassword';
  const phoneNumber = '51111222';

  /* Helpers */
  const expectCopy = () => {
    expect(screen.getByText('Rebrowse')).toBeInTheDocument();
    expect(screen.getByText('Start your free trial now.')).toBeInTheDocument();
    expect(
      screen.getByText("You're minutes away from insights.")
    ).toBeInTheDocument();

    const helpButton = screen.getByText('Help');
    expect(helpButton.parentElement?.getAttribute('href')).toEqual(helpBaseURL);

    const logInButton = screen.getByText('Log in');
    expect(logInButton.parentElement?.getAttribute('href')).toEqual(appBaseURL);
  };

  test('as a user I can start a free trial', async () => {
    /* Mocks */
    const signupCreateStub = sandbox.stub(sdk.signup, 'create').resolves();

    /* Server */
    const { page } = await getPage({ route: '/' });

    /* Client */
    render(page);
    expectCopy();

    const submitButton = screen.getByText('Get started');
    const fullNameInput = screen.getByPlaceholderText('John Doe');
    const companyInput = screen.getByPlaceholderText('Example');
    const emailInput = screen.getByPlaceholderText('john.doe@gmail.com');
    const passwordInput = screen.getByPlaceholderText('Password');
    const phoneNumberInput = screen.getByPlaceholderText('51111222');

    userEvent.type(fullNameInput, fullName);
    userEvent.type(companyInput, company);
    userEvent.type(emailInput, email);
    userEvent.type(passwordInput, password);
    userEvent.type(phoneNumberInput, phoneNumber);

    userEvent.click(submitButton);

    await screen.findByText(
      'We have sent an email with a confirmation link to your email address.'
    );

    sandbox.assert.calledWithExactly(signupCreateStub, {
      fullName,
      company,
      email,
      password,
      phoneNumber: { countryCode: '+1', digits: phoneNumber },
    });
  });

  test('As a user I can start a free trial without my phone number', async () => {
    /* Mocks */
    const signupCreateStub = sandbox.stub(sdk.signup, 'create').resolves();

    /* Server */
    const { page } = await getPage({ route: '/' });

    /* Client */
    render(page);
    expectCopy();

    const submitButton = screen.getByText('Get started');
    const fullNameInput = screen.getByPlaceholderText('John Doe');
    const companyInput = screen.getByPlaceholderText('Example');
    const emailInput = screen.getByPlaceholderText('john.doe@gmail.com');
    const passwordInput = screen.getByPlaceholderText('Password');

    userEvent.click(submitButton);
    expect((await screen.findAllByText('Required')).length).toEqual(4);

    userEvent.type(fullNameInput, fullName);
    userEvent.type(companyInput, company);
    userEvent.type(emailInput, 'random');
    userEvent.type(passwordInput, 'short');

    userEvent.click(submitButton);
    await screen.findByText('Invalid email address');
    await screen.findByText('Password must be at least 8 characters long');

    userEvent.clear(emailInput);
    userEvent.type(emailInput, email);
    userEvent.clear(passwordInput);
    userEvent.type(passwordInput, password);

    userEvent.click(submitButton);

    await screen.findByText(
      'We have sent an email with a confirmation link to your email address.'
    );

    sandbox.assert.calledWithExactly(signupCreateStub, {
      fullName,
      company,
      email,
      password,
      phoneNumber: undefined,
    });
  });
});
