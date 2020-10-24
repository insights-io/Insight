import { queryByPlaceholderText, queryByText } from '@testing-library/testcafe';

import { ACCEPT_INVITE_PAGE } from '../../src/shared/constants/routes';

import { AbstractPage } from './AbstractPage';

class AcceptTeamInvitePage extends AbstractPage {
  public readonly fullNameInput = queryByPlaceholderText('Full name');
  public readonly passwordInput = queryByPlaceholderText('Password');
  public readonly submitButton = queryByText('Continue');
}

export default new AcceptTeamInvitePage(ACCEPT_INVITE_PAGE);
