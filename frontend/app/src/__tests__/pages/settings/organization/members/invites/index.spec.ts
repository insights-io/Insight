import { sandbox } from '@rebrowse/testing';
import {
  render,
  screen,
  waitForElementToBeRemoved,
} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { ORGANIZATION_SETTINGS_MEMBER_INVITES_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import {
  ADMIN_TEAM_INVITE_DTO,
  EXPIRED_TEAM_INVITE_DTO,
  STANDARD_TEAM_INVITE_DTO,
} from '__tests__/data';
import { mockOrganizationSettingsMemberInvitesPage } from '__tests__/mocks';

describe('/settings/organization/members', () => {
  /* Data */
  const route = ORGANIZATION_SETTINGS_MEMBER_INVITES_PAGE;
  const invites = [
    ADMIN_TEAM_INVITE_DTO,
    EXPIRED_TEAM_INVITE_DTO,
    STANDARD_TEAM_INVITE_DTO,
  ];
  const randomQuery = 'random';
  const emailQuery = invites[2].email.substring(3, 6);

  test('As a user I want to search for team invites in my organization', async () => {
    document.cookie = 'SessionId=123';
    const {
      listTeamInvitesStub,
      countTeamInvitesStub,
    } = mockOrganizationSettingsMemberInvitesPage(sandbox, { invites });

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(listTeamInvitesStub, {
      baseURL: 'http://localhost:8080',
      headers: { cookie: 'SessionId=123', 'uber-trace-id': match.string },
      search: { limit: 20, sortBy: ['+createdAt'] },
    });
    sandbox.assert.calledWithExactly(countTeamInvitesStub, {
      baseURL: 'http://localhost:8080',
      headers: { cookie: 'SessionId=123', 'uber-trace-id': match.string },
    });

    /* Client */
    render(page);

    expect(screen.getByText(invites[0].email)).toBeInTheDocument();
    expect(screen.getByText(invites[1].email)).toBeInTheDocument();
    expect(screen.getByText(invites[2].email)).toBeInTheDocument();

    const searchInput = screen.getByPlaceholderText('Search invites');
    userEvent.type(searchInput, randomQuery);

    await waitForElementToBeRemoved(() => screen.queryByText(invites[0].email));
    expect(screen.queryByText(invites[1].email)).toBeNull();
    expect(screen.queryByText(invites[2].email)).toBeNull();

    sandbox.assert.calledWithExactly(listTeamInvitesStub, {
      search: { limit: 20, sortBy: ['+createdAt'], query: randomQuery },
    });

    userEvent.clear(searchInput);
    userEvent.type(searchInput, emailQuery);
    await screen.findByText(invites[2].email);

    sandbox.assert.calledWithExactly(listTeamInvitesStub, {
      search: { limit: 20, sortBy: ['+createdAt'], query: emailQuery },
    });
  });
});
