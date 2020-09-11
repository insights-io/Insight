import { queryByPlaceholderText, queryByText } from '@testing-library/testcafe';

class TeamInvite {
  public readonly inviteNewMember = queryByText('Invite new member');

  public readonly emailInput = queryByPlaceholderText('Email');
  public readonly invite = queryByText('Invite');
  public readonly invitedMessage = queryByText('Member invited');

  public readonly role = {
    admin: queryByText('Admin'),
  };
}

export default new TeamInvite();
