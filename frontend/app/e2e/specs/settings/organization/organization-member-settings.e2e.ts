/* eslint-disable no-await-in-loop */
import { queryByText } from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import {
  Sidebar,
  SignUpPage,
  OrganizationGeneralSettingsPage,
  OrganizationMembersSettingsPage,
} from '../../../pages';

fixture('/settings/organization/members/invites').page(
  OrganizationGeneralSettingsPage.path
);

test('As a user I should be able to invite multiple users and then search invites by query', async (t) => {
  const credentials = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, credentials);

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.organization.members)
    .click(OrganizationMembersSettingsPage.tabs.teamInvites);

  const {
    teamInviteList,
    teamInviteSearchInput,
  } = OrganizationMembersSettingsPage;

  for (let i = 0; i < 3; i++) {
    const email = `${uuid()}+${i}@gmail.com`;

    const {
      inviteNewTeamMemberButton,
      inviteNewMemberModal,
    } = OrganizationMembersSettingsPage;

    await t
      .click(inviteNewTeamMemberButton)
      .typeText(inviteNewMemberModal.emailInput, email)
      .click(inviteNewMemberModal.role.admin)
      .click(inviteNewMemberModal.inviteButton)
      .expect(queryByText(email).visible)
      .ok('Should display user in lsit')
      .expect(teamInviteList.childElementCount)
      .eql(i + 1, 'Count should increase');
  }

  await t
    .typeText(teamInviteSearchInput, '+1')
    .expect(teamInviteList.childElementCount)
    .eql(1, 'Should only show matched team invites')
    .selectText(teamInviteSearchInput)
    .pressKey('delete')
    .expect(teamInviteList.childElementCount)
    .eql(3, 'Should show all team invites');
});
