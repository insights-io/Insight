import { queryByText } from '@testing-library/testcafe';

export class DisableTwoFactorAuthenticationModal {
  public readonly confirmButton = queryByText('Yes');
  public readonly cancelButton = queryByText('Cancel');
}

export default new DisableTwoFactorAuthenticationModal();
