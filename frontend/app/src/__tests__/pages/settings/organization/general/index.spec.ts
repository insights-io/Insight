import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { INCLUDE_CREDENTIALS } from 'sdk';
import { ORGANIZATION_SETTINGS_GENERAL_PAGE } from 'shared/constants/routes';
import { REBROWSE_ORGANIZATION } from '__tests__/data/organization';
import { mockOrganizationSettingsGeneralPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/settings/organization/general', () => {
  /* Data */
  const route = ORGANIZATION_SETTINGS_GENERAL_PAGE;

  it('As a user I can see & edit general organization settigns', async () => {
    document.cookie = 'SessionId=123';
    const { updateOrganizationStub } = mockOrganizationSettingsGeneralPage(
      sandbox
    );

    /* Server */
    const { page } = await getPage({ route });

    /* Client */
    renderPage(page);

    expect(
      screen.getByDisplayValue(REBROWSE_ORGANIZATION.id)
    ).toBeInTheDocument();

    const organizationNameInput = screen.getByDisplayValue(
      REBROWSE_ORGANIZATION.name as string
    ) as HTMLInputElement;

    userEvent.type(organizationNameInput, '-extra');
    userEvent.tab(); // trigger blur event

    const updatedName = `${REBROWSE_ORGANIZATION.name}-extra`;
    expect(organizationNameInput.value).toEqual(updatedName);

    await screen.findByText(
      `Successfully changed organization name from "${REBROWSE_ORGANIZATION.name}" to "${updatedName}"`
    );

    sandbox.assert.calledWithExactly(
      updateOrganizationStub,
      { name: updatedName },
      INCLUDE_CREDENTIALS
    );

    const openMembershipToggle = screen
      .getByText(
        'Allow users to freely join the organization using SSO instead of requiring an invite'
      )
      .parentElement?.parentElement?.parentElement?.querySelector(
        'input'
      ) as HTMLInputElement;
    expect(openMembershipToggle).not.toBeChecked();

    userEvent.click(openMembershipToggle);
    await screen.findByText(
      'Successfully enabled organization open membership'
    );

    expect(openMembershipToggle).toBeChecked();
    sandbox.assert.calledWithExactly(
      updateOrganizationStub,
      { openMembership: true },
      INCLUDE_CREDENTIALS
    );
  });
});
