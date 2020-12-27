/* eslint-disable max-classes-per-file */
import { queryByText, within } from '@testing-library/testcafe';

import { EMAIL_PLACEHOLDER } from '../../../../src/shared/constants/form-placeholders';
import { ORGANIZATION_SETTINGS_MEMBERS_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

class InviteNewMemberModal {
  private readonly container = queryByText('Invite new member').parent().nth(0);
  private readonly withinContainer = within(this.container);

  public readonly header = this.withinContainer.queryByText(
    'Invite new member'
  );
  public readonly emailInput = this.withinContainer.queryByPlaceholderText(
    EMAIL_PLACEHOLDER
  );
  public readonly inviteButton = this.withinContainer.queryByText('Invite');
  public readonly cancelButton = this.withinContainer.queryByText('Cancel');

  public readonly invitedMessage = queryByText('Member invited');

  public readonly role = {
    admin: this.withinContainer.queryByText('Admin'),
    member: this.withinContainer.queryByText('Member'),
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

  private readonly tabpanel = this.withinContainer.queryByRole('tabpanel');
  private readonly withinTabpanel = within(this.tabpanel);

  public readonly teamInviteList = this.tabpanel.find('ul');
  public readonly teamInviteSearchInput = this.withinTabpanel.queryByPlaceholderText(
    'Search invites'
  );

  public readonly inviteNewTeamMemberButton = this.withinContainer.queryByText(
    'Invite member'
  );

  public readonly inviteNewMemberModal = new InviteNewMemberModal();
}

export default new OrganizationMembersSettingsPage(
  ORGANIZATION_SETTINGS_MEMBERS_PAGE
);
