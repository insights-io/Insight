import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import { client } from 'sdk';
import { PASSWORD_FORGOT_ROUTE } from 'shared/constants/routes';
import userEvent from '@testing-library/user-event';
import type { Awaited } from 'shared/utils/types';
import { getPage } from '__tests__/utils';

describe('/password-forgot', () => {
  const setup = async () => {
    const { render } = await getPage({ route: PASSWORD_FORGOT_ROUTE });
    render();

    expect(
      screen.getByRole('heading', { name: 'Forgot password?' })
    ).toBeInTheDocument();

    expect(
      screen.getByRole('heading', {
        name:
          "Enter your email below and we'll send you a link to reset your password.",
      })
    ).toBeInTheDocument();

    // links
    const rememberPasswordLink = screen.getByRole('link', {
      name: 'Remember password?',
    });

    // inputs
    const emailInput = screen.getByPlaceholderText('john.doe@gmail.com');

    // buttons
    const continueButton = screen.getByRole('button', { name: 'Continue' });

    return { rememberPasswordLink, emailInput, continueButton };
  };

  describe('As a user I want the page to be accessible', () => {
    test('Navigation via tabbing', async () => {
      const {
        emailInput,
        continueButton,
        rememberPasswordLink,
      } = await setup();

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
      submitHandler: (setupResult: Awaited<ReturnType<typeof setup>>) => void
    ) => {
      const passwordForgotStub = sandbox
        .stub(client.password, 'forgot')
        .resolves();

      const setupResult = await setup();
      const { emailInput } = setupResult;

      userEvent.type(emailInput, email);

      submitHandler(setupResult);

      await screen.findByRole('heading', { name: 'Check your inbox!' });
      expect(
        screen.getByRole('heading', {
          name:
            'If your email address is associated with an Rebrowse account, you will be receiving a password reset request shortly.',
        })
      ).toBeInTheDocument();

      sandbox.assert.calledWithExactly(passwordForgotStub, email);
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
