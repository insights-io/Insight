import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@rebrowse/testing';
import { client } from 'sdk';
import { appBaseUrl } from 'shared/config';

import * as SignUpConfirmTestSetup from '../signup-confirm/SignupConfirmPageSetup';

import { setup } from './SignUpPageTestSetup';

describe('/signup', () => {
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
      const signupCreateStub = sandbox.stub(client.signup, 'create').resolves();

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

      await SignUpConfirmTestSetup.findElements();

      sandbox.assert.calledWithExactly(signupCreateStub, {
        fullName,
        company,
        email,
        password,
        phoneNumber: { countryCode: '+1', digits: phoneNumber },
        redirect: appBaseUrl,
      });
    });

    test('By omitting my phone number', async () => {
      /* Mocks */
      const signupCreateStub = sandbox.stub(client.signup, 'create').resolves();

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

      await SignUpConfirmTestSetup.findElements();

      sandbox.assert.calledWithExactly(signupCreateStub, {
        fullName,
        company,
        email,
        password,
        phoneNumber: undefined,
        redirect: appBaseUrl,
      });
    });
  });
});
