import { screen, waitFor } from '@testing-library/react';
import { SIGNUP_ROUTE } from 'shared/constants/routes';
import { getPage } from '__tests__/utils';

export const MATCHERS = {
  heading: ['heading', { name: 'Start your free trial now.' }],
  subheading: ['heading', { name: "You're minutes away from insights." }],
  fullNameInput: ['John Doe'],
  companyInput: ['Example'],
  emailInput: ['john.doe@gmail.com'],
  passwordInput: ['Password'],
  phoneNumberInput: ['51111222'],
  submitButton: ['button', { name: 'Get started' }],
  countryInput: ['combobox', { name: 'Select country' }],
  showPasswordButton: ['button', { name: 'Show password text' }],
  logInLink: ['link', { name: 'Log in' }],
} as const;

export const setup = async (route = SIGNUP_ROUTE) => {
  const { render } = await getPage({ route });
  render();
  return getElements();
};

export const findElements = () => {
  return waitFor(() => getElements());
};

export const getElements = () => {
  return {
    heading: screen.getByRole(...MATCHERS.heading),
    subheading: screen.getByRole(...MATCHERS.subheading),
    fullNameInput: screen.getByPlaceholderText(...MATCHERS.fullNameInput),
    companyInput: screen.getByPlaceholderText(...MATCHERS.companyInput),
    emailInput: screen.getByPlaceholderText(...MATCHERS.emailInput),
    passwordInput: screen.getByPlaceholderText(...MATCHERS.passwordInput),
    phoneNumberInput: screen.getByPlaceholderText(...MATCHERS.phoneNumberInput),
    submitButton: screen.getByRole(...MATCHERS.submitButton),
    countryInput: screen.getByRole(...MATCHERS.countryInput),
    showPasswordButton: screen.getByRole(...MATCHERS.showPasswordButton),
    logInLink: screen.getByRole(...MATCHERS.logInLink),
  };
};
