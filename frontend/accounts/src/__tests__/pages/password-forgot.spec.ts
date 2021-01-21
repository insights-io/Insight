import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { PASSWORD_FORGOT_ROUTE } from 'shared/constants/routes';

describe('/password-forgot', () => {
  const setup = async () => {
    const { render } = await getPage({
      route: PASSWORD_FORGOT_ROUTE,
      useApp: false,
    });
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
});
