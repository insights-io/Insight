/* eslint-disable max-classes-per-file */
import {
  queryByPlaceholderText,
  queryByText,
  within,
} from '@testing-library/testcafe';

import { ORGANIZATION_SETTINGS_MEMBERS_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

class InviteNewMemberModal {
  public readonly header = queryByText('Invite new member');
  public readonly emailInput = queryByPlaceholderText('Email');
  public readonly inviteButton = queryByText('Invite');
  public readonly cancelButton = queryByText('Cancel');
  public readonly invitedMessage = queryByText('Member invited');

  public readonly role = {
    admin: queryByText('Admin'),
    member: queryByText('Member'),
  };
}

export class OrganizationMembersSettingsPage extends AbstractOrganizationSettingsPage {
  public readonly header = this.withinContainer.queryByText('Members');

  private readonly tablist = within(
    this.withinContainer.queryByRole('tablist')
  );

  public readonly tabs = {
    members: this.tablist.queryByText('Members'),
    teamInvites: this.tablist.queryByText('Team invites'),
  };

  public readonly inviteNewTeamMemberButton = this.withinContainer.queryByText(
    'Invite new member'
  );

  public readonly inviteNewMemberModal = new InviteNewMemberModal();
}

export default new OrganizationMembersSettingsPage(
  ORGANIZATION_SETTINGS_MEMBERS_PAGE
);
