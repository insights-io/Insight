import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { INDEX_ROUTE } from 'shared/constants/routes';

describe('/', () => {
  const setup = async () => {
    const { render } = await getPage({ route: INDEX_ROUTE, useApp: false });
    render();

    // TODO: SEO

    expect(
      screen.getByRole('heading', { name: 'Sign in to Rebrowse' })
    ).toBeInTheDocument();

    // inputs
    const emailInput = screen.getByPlaceholderText('john.doe@gmail.com');

    // buttons
    const continueButton = screen.getByRole('button', { name: 'Continue' });

    // links
    const createFreeAcountLink = screen.getByRole('link', {
      name: 'Create a free account',
    });
    const joinAnExistingTeamLink = screen.getByRole('link', {
      name: 'Join an existing team',
    });

    expect(screen.getByText('Or')).toBeInTheDocument();

    const signInWithGoogle = screen.getByRole('link', {
      name: /Sign in with Google/,
    });
    const signInWithGithub = screen.getByRole('link', {
      name: 'Sign in with Github',
    });
    const signInWithMicrosoft = screen.getByRole('link', {
      name: 'Sign in with Microsoft',
    });

    return {
      emailInput,
      continueButton,
      createFreeAcountLink,
      joinAnExistingTeamLink,
      signInWithGoogle,
      signInWithGithub,
      signInWithMicrosoft,
    };
  };

  test('As a user I can navigate between "Password forgot?" and login without using email', async () => {
    const { emailInput, continueButton } = await setup();

    const email = 'john.doe@gmail.com';
    userEvent.type(emailInput, email);
    userEvent.click(continueButton);
    userEvent.click(await screen.findByRole('link', { name: 'Forgot?' }));

    // password-forgot page
    await screen.findByRole('heading', { name: 'Forgot password?' });
    expect(
      screen.getByRole('heading', {
        name:
          "Enter your email below and we'll send you a link to reset your password.",
      })
    ).toBeInTheDocument();

    // input is prefilled
    expect(screen.getByDisplayValue(email)).toBeInTheDocument();

    userEvent.click(screen.getByRole('link', { name: 'Remember password?' }));

    // index page
    await screen.findByRole('heading', { name: 'Sign in to Rebrowse' });
    expect(screen.getByDisplayValue(email)).toBeInTheDocument();
  });

  describe('As a user I want the page to be accessible', () => {
    test('Navigation via tabbing', async () => {
      const {
        emailInput,
        continueButton,
        createFreeAcountLink,
        joinAnExistingTeamLink,
        signInWithGoogle,
        signInWithGithub,
        signInWithMicrosoft,
      } = await setup();

      expect(document.activeElement).toEqual(emailInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(continueButton);
      userEvent.tab();
      expect(document.activeElement).toEqual(createFreeAcountLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(joinAnExistingTeamLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithGoogle);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithGithub);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithMicrosoft);
      userEvent.tab();
      expect(document.activeElement).toEqual(document.body);
      userEvent.tab();
      expect(document.activeElement).toEqual(emailInput);

      userEvent.type(emailInput, 'john.doe@gmail.com');
      userEvent.click(continueButton);

      const passwordInput = await screen.findByPlaceholderText('Password');
      const passwordForgotLink = screen.getByRole('link', { name: 'Forgot?' });
      const showPasswordButton = screen.getByRole('button', {
        name: 'Show password text',
      });

      expect(document.activeElement).toEqual(passwordInput);

      userEvent.tab({ shift: true });
      expect(document.activeElement).toEqual(passwordForgotLink);
      userEvent.tab();
      userEvent.tab();
      expect(document.activeElement).toEqual(showPasswordButton);
    });
  });
});
