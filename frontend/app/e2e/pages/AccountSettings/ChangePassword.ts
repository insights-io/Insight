import { queryByPlaceholderText, queryByText } from '@testing-library/testcafe';

type ChangePasswordParams = {
  currentPassword: string;
  newPassword: string;
  newPasswordConfirm: string;
};

class ChangePassword {
  public readonly currentPasswordInput = queryByPlaceholderText(
    'Current password'
  );
  public readonly newPasswordInput = queryByPlaceholderText('New password');
  public readonly confirmNewPasswordInput = queryByPlaceholderText(
    'Confirm new password'
  );
  public readonly saveNewPasswordButton = queryByText('Save new password');
  public readonly passwordMissmatchErrorMessage = queryByText(
    'Current password miss match'
  );
  public readonly passwordChangedMessage = queryByText('Password changed');
  public readonly newPasswordSameAsOldErrorMessage = queryByText(
    'New password cannot be the same as the previous one!'
  );

  public clearInputs = (t: TestController) => {
    return t
      .selectText(this.currentPasswordInput)
      .pressKey('delete')
      .selectText(this.newPasswordInput)
      .pressKey('delete')
      .selectText(this.confirmNewPasswordInput)
      .pressKey('delete');
  };

  public changePassword = (
    t: TestController,
    { currentPassword, newPassword, newPasswordConfirm }: ChangePasswordParams
  ) => {
    return t
      .typeText(this.currentPasswordInput, currentPassword)
      .typeText(this.newPasswordInput, newPassword)
      .typeText(this.confirmNewPasswordInput, newPasswordConfirm)
      .click(this.saveNewPasswordButton);
  };
}

export default new ChangePassword();
