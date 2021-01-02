import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { ORGANIZATION_SETTINGS_SECURITY_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { mockOrganizationSettingsSecurityPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/settings/organization/security', () => {
  /* Data */
  const route = ORGANIZATION_SETTINGS_SECURITY_PAGE;

  test('As a user I want to configure password policy for my organization', async () => {
    document.cookie = 'SessionId=123';
    const {
      retrievePasswordPolicyStub,
      createPasswordPolicyStub,
      updatePasswordPolicyStub,
    } = mockOrganizationSettingsSecurityPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(retrievePasswordPolicyStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    const { container } = renderPage(page);

    expect(
      container.querySelector('input[name="preventPasswordReuse"]')
    ).toBeChecked();

    userEvent.click(
      screen.getByText(
        'Require at least one uppercase letter from Latin alphabet (A-Z)'
      )
    );

    expect(
      container.querySelector('input[name="requireUppercaseCharacter"]')
    ).toBeChecked();

    userEvent.click(screen.getByText('Save'));

    await screen.findByText('Password policy updated');

    sandbox.assert.calledWithExactly(createPasswordPolicyStub, {
      minCharacters: 8,
      preventPasswordReuse: true,
      requireLowercaseCharacter: false,
      requireNonAlphanumericCharacter: false,
      requireNumber: false,
      requireUppercaseCharacter: true,
    });

    userEvent.click(
      screen.getByText(
        'Require at least one uppercase letter from Latin alphabet (A-Z)'
      )
    );
    userEvent.type(screen.getByPlaceholderText('Min characters'), '0');

    expect(
      container.querySelector('input[name="requireUppercaseCharacter"]')
    ).not.toBeChecked();

    userEvent.click(screen.getByText('Save'));

    await screen.findByText('Password policy updated');

    sandbox.assert.calledWithExactly(updatePasswordPolicyStub, {
      minCharacters: 80,
      preventPasswordReuse: true,
      requireLowercaseCharacter: false,
      requireNonAlphanumericCharacter: false,
      requireNumber: false,
      requireUppercaseCharacter: false,
    });
  });
});
