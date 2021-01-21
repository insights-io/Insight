import { screen } from '@testing-library/react';
import { getPage } from 'next-page-tester';
import { helpBaseURL } from 'shared/config';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@rebrowse/testing';
import { sdk } from 'api';
import { SIGNUP_ROUTE } from 'shared/constants/routes';

describe('/signup', () => {
  const setup = async () => {
    const { render } = await getPage({ route: SIGNUP_ROUTE, useApp: false });
    render();

    // TODO: SEO

    // Topbar heading
    expect(
      screen.getByRole('heading', { name: 'Rebrowse' })
    ).toBeInTheDocument();

    // Content headings
    expect(
      screen.getByRole('heading', { name: 'Start your free trial now.' })
    ).toBeInTheDocument();
    expect(
      screen.getByRole('heading', {
        name: "You're minutes away from insights.",
      })
    ).toBeInTheDocument();

    // inputs
    const fullNameInput = screen.getByPlaceholderText('John Doe');
    const companyInput = screen.getByPlaceholderText('Example');
    const emailInput = screen.getByPlaceholderText('john.doe@gmail.com');
    const passwordInput = screen.getByPlaceholderText('Password');
    const phoneNumberInput = screen.getByPlaceholderText('51111222');
    const countryInput = screen.getByRole('combobox', {
      name: 'Select country',
    });

    // buttons
    const submitButton = screen.getByRole('button', { name: 'Get started' });
    const showPasswordButton = screen.getByRole('button', {
      name: 'Show password text',
    });

    // links
    const logInLink = screen.getByRole('link', { name: 'Log in' });
    const helpLink = screen.getByRole('link', { name: 'Help' });
    expect(helpLink).toHaveAttribute('href', helpBaseURL);

    return {
      fullNameInput,
      companyInput,
      emailInput,
      passwordInput,
      phoneNumberInput,
      submitButton,
      countryInput,
      showPasswordButton,
      logInLink,
      helpLink,
    };
  };

  describe('As a user I want the page to be accessible', () => {
    test('Navigation via tabbing', async () => {
      const {
        fullNameInput,
        companyInput,
        phoneNumberInput,
        emailInput,
        passwordInput,
        submitButton,
        countryInput,
        showPasswordButton,
        logInLink,
        helpLink,
      } = await setup();

      expect(document.activeElement).toEqual(fullNameInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(companyInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(countryInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(phoneNumberInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(emailInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(passwordInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(showPasswordButton);
      userEvent.tab();
      expect(document.activeElement).toEqual(submitButton);
      userEvent.tab();
      expect(document.activeElement).toEqual(logInLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(document.body);
      userEvent.tab();
      expect(document.activeElement).toEqual(helpLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(fullNameInput);
    });
  });

  describe('As a user I can start a free trial', () => {
    /* Data */
    const fullName = 'Joe Makarena';
    const company = 'Rebrowse';
    const email = 'user@example.com';
    const password = 'veryHardPassword';
    const phoneNumber = '51111222';

    test('By filling in all details', async () => {
      /* Mocks */
      const signupCreateStub = sandbox.stub(sdk.signup, 'create').resolves();

      /* Render */
      const {
        fullNameInput,
        companyInput,
        emailInput,
        passwordInput,
        phoneNumberInput,
        submitButton,
      } = await setup();

      userEvent.type(fullNameInput, fullName);
      userEvent.type(companyInput, company);
      userEvent.type(emailInput, email);
      userEvent.type(passwordInput, password);
      userEvent.type(phoneNumberInput, phoneNumber);
      userEvent.click(submitButton);

      await screen.findByRole('heading', {
        name:
          'We have sent an email with a confirmation link to your email address.',
      });

      sandbox.assert.calledWithExactly(signupCreateStub, {
        fullName,
        company,
        email,
        password,
        phoneNumber: { countryCode: '+1', digits: phoneNumber },
      });
    });

    test('By omitting my phone number', async () => {
      /* Mocks */
      const signupCreateStub = sandbox.stub(sdk.signup, 'create').resolves();

      /* Render */
      const {
        submitButton,
        fullNameInput,
        companyInput,
        emailInput,
        passwordInput,
      } = await setup();

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

      await screen.findByRole('heading', {
        name:
          'We have sent an email with a confirmation link to your email address.',
      });

      sandbox.assert.calledWithExactly(signupCreateStub, {
        fullName,
        company,
        email,
        password,
        phoneNumber: undefined,
      });
    });
  });
});
