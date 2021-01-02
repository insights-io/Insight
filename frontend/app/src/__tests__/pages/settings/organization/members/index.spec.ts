import { sandbox } from '@rebrowse/testing';
import { screen, waitForElementToBeRemoved } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { ORGANIZATION_SETTINGS_MEMBERS_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { REBROWSE_ADMIN_DTO } from '__tests__/data';
import { mockOrganizationSettingsMembersPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/settings/organization/members', () => {
  /* Data */
  const route = ORGANIZATION_SETTINGS_MEMBERS_PAGE;
  const randomQuery = 'random';
  const emailQuery = REBROWSE_ADMIN_DTO.email.substring(3, 6);

  test('As a user I want to search for members in my organization', async () => {
    document.cookie = 'SessionId=123';
    const {
      listMembersStub,
      countMembersStub,
    } = mockOrganizationSettingsMembersPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(listMembersStub, {
      headers: { cookie: 'SessionId=123', 'uber-trace-id': match.string },
      search: { limit: 20, sortBy: ['+createdAt'] },
    });
    sandbox.assert.calledWithExactly(countMembersStub, {
      headers: { cookie: 'SessionId=123', 'uber-trace-id': match.string },
    });

    /* Client */
    renderPage(page);

    expect(
      screen.getByText(REBROWSE_ADMIN_DTO.fullName as string)
    ).toBeInTheDocument();
    expect(screen.getByText(REBROWSE_ADMIN_DTO.email)).toBeInTheDocument();

    const searchInput = screen.getByPlaceholderText('Search members');
    userEvent.type(searchInput, randomQuery);

    await waitForElementToBeRemoved(() =>
      screen.queryByText(REBROWSE_ADMIN_DTO.email)
    );

    sandbox.assert.calledWithExactly(listMembersStub, {
      search: { limit: 20, sortBy: ['+createdAt'], query: randomQuery },
    });

    userEvent.clear(searchInput);
    userEvent.type(searchInput, emailQuery);
    await screen.findByText(REBROWSE_ADMIN_DTO.email);

    sandbox.assert.calledWithExactly(listMembersStub, {
      search: { limit: 20, sortBy: ['+createdAt'], query: emailQuery },
    });
  });
});
