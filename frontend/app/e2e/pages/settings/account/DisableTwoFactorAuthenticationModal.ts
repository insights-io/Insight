import { queryByText } from '@testing-library/testcafe';

export class DisableMfaModal {
  public readonly confirmButton = queryByText('Disable');
  public readonly cancelButton = queryByText('Maybe later');
}

export default new DisableMfaModal();
