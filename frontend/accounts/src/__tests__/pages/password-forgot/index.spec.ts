import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { INCLUDE_CREDENTIALS } from 'sdk';
import { appBaseUrl } from 'shared/config';
import type { Awaited } from 'shared/utils/types';

import * as PasswordForgotPageTestSetup from './PasswordForgotPageTestSetup';

describe('/password-forgot', () => {
  describe('As a user I want the page to be accessible', () => {
    test('Navigation via tabbing', async () => {
      const {
        emailInput,
        continueButton,
        rememberPasswordLink,
      } = await PasswordForgotPageTestSetup.setup();

      expect(document.activeElement).toEqual(emailInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(continueButton);
      userEvent.tab();
      expect(document.activeElement).toEqual(document.body);
      userEvent.tab();
      expect(document.activeElement).toEqual(rememberPasswordLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(emailInput);
    });
  });

  describe('As a user I can forget my password', () => {
    const email = 'john.doe@gmail.com';

    const passwordForgotFlow = async (
      submit: (
        setupResult: Awaited<
          ReturnType<typeof PasswordForgotPageTestSetup['setup']>
        >
      ) => void
    ) => {
      const passwordForgotStub = PasswordForgotPageTestSetup.passwordForgotStub();
      const setupResult = await PasswordForgotPageTestSetup.setup();
      const { emailInput } = setupResult;

      userEvent.type(emailInput, email);

      submit(setupResult);

      await screen.findByRole('heading', { name: 'Check your inbox!' });
      expect(
        screen.getByRole('heading', {
          name:
            'If your email address is associated with an Rebrowse account, you will be receiving a password reset request shortly.',
        })
      ).toBeInTheDocument();

      sandbox.assert.calledWithExactly(
        passwordForgotStub,
        { email, redirect: appBaseUrl },
        INCLUDE_CREDENTIALS
      );
    };

    test('Using onClick', async () => {
      await passwordForgotFlow(({ continueButton }) =>
        userEvent.click(continueButton)
      );
    });

    test('{enter} keypress', async () => {
      await passwordForgotFlow(({ emailInput }) =>
        userEvent.type(emailInput, '{enter}')
      );
    });
  });
});
