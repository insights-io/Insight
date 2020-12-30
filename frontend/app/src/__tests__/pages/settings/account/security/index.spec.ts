import { sandbox } from '@rebrowse/testing';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { ACCOUNT_SETTINGS_SECURITY_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { mockAccountSettingsSecurityPage } from '__tests__/mocks';

describe('/settings/account/security', () => {
  /* Data */
  const route = ACCOUNT_SETTINGS_SECURITY_PAGE;

  describe('Password change', () => {
    const currentPassword = 'password1234';
    const newPassword = `${currentPassword}!`;

    test('As a user I can change my password', async () => {
      /* Mocks */
      document.cookie = 'SessionId=123';
      const {
        listMfaSetupsStub,
        changePasswordStub,
      } = mockAccountSettingsSecurityPage(sandbox);

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithExactly(listMfaSetupsStub, {
        baseURL: 'http://localhost:8080',
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
      });

      /* Client */
      render(page);

      userEvent.type(
        screen.getByPlaceholderText('Current password'),
        currentPassword
      );
      userEvent.type(screen.getByPlaceholderText('New password'), newPassword);
      userEvent.type(
        screen.getByPlaceholderText('Confirm new password'),
        newPassword
      );

      userEvent.click(screen.getByText('Save new password'));

      await screen.findByText('Password changed');
      sandbox.assert.calledWithExactly(changePasswordStub, {
        currentPassword,
        newPassword,
        confirmNewPassword: newPassword,
      });
    });
  });

  describe('Multi-factor authentication', () => {
    describe('TOTP', () => {
      test('As a user I can disable TOTP multi-factor authentication setup', async () => {
        /* Mocks */
        document.cookie = 'SessionId=123';
        const {
          listMfaSetupsStub,
          disableMfaSetupStub,
        } = mockAccountSettingsSecurityPage(sandbox);

        /* Server */
        const { page } = await getPage({ route });

        sandbox.assert.calledWithExactly(listMfaSetupsStub, {
          baseURL: 'http://localhost:8080',
          headers: {
            cookie: 'SessionId=123',
            'uber-trace-id': (match.string as unknown) as string,
          },
        });

        /* Client */
        render(page);

        const authyMfaToggle = screen
          .getByText(
            'Use your favorite app to generate a time-dependent six-digit code'
          )
          .parentElement?.parentElement?.parentElement?.querySelector(
            'input'
          ) as HTMLInputElement;

        expect(authyMfaToggle).toBeChecked();

        userEvent.click(authyMfaToggle);
        expect(
          screen.getByText(
            'Are you sure you want to disable multi-factor authentication method?'
          )
        ).toBeInTheDocument();
        userEvent.click(screen.getByText('Disable'));
        await screen.findByText(
          'Authy / Google Authenticator multi-factor authentication disabled'
        );
        sandbox.assert.calledWithExactly(disableMfaSetupStub, 'totp');
        expect(authyMfaToggle).not.toBeChecked();

        expect(
          screen.queryByText(
            'Are you sure you want to disable multi-factor authentication method?'
          )
        ).toBeNull();
      });

      test('as a user I can enable TOTP multi-factor authentication setup', async () => {
        /* Mocks */
        document.cookie = 'SessionId=123';
        const {
          listMfaSetupsStub,
          startMfaTotpSetupStub,
          completeMfaSetupStub,
        } = mockAccountSettingsSecurityPage(sandbox, { mfaSetups: [] });

        /* Server */
        const { page } = await getPage({ route });

        sandbox.assert.calledWithExactly(listMfaSetupsStub, {
          baseURL: 'http://localhost:8080',
          headers: {
            cookie: 'SessionId=123',
            'uber-trace-id': (match.string as unknown) as string,
          },
        });

        /* Client */
        render(page);

        const authyMfaToggle = screen
          .getByText(
            'Use your favorite app to generate a time-dependent six-digit code'
          )
          .parentElement?.parentElement?.parentElement?.querySelector(
            'input'
          ) as HTMLInputElement;

        expect(authyMfaToggle).not.toBeChecked();
        userEvent.click(authyMfaToggle);
        expect(
          screen.getByText('Setup multi-factor authentication')
        ).toBeInTheDocument();

        sandbox.assert.calledWithExactly(startMfaTotpSetupStub);

        screen
          .getAllByLabelText('Please enter your pin code')
          .forEach((input) => {
            userEvent.type(input, '1');
          });

        userEvent.click(screen.getByText('Submit'));
        await screen.findByText(
          'Authy / Google Authenticator multi-factor authentication enabled'
        );
        expect(authyMfaToggle).toBeChecked();
        sandbox.assert.calledWithExactly(completeMfaSetupStub, 'totp', 111111);
      });
    });
  });
});
