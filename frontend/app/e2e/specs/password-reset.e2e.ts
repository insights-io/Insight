import { v4 as uuid } from 'uuid';

import { LoginPage, PasswordResetPage } from '../pages';
import { getLocation } from '../utils';

fixture('/password-reset').page(`${PasswordResetPage.path}?token=${uuid()}`);

test('User should see a nice error message on password reset with invalid token', async (t) => {
  await t
    .expect(PasswordResetPage.passwordResetRequestNotFoundMessage.visible)
    .ok('Should display nice message on password-reset with invalid token')
    .click(PasswordResetPage.loginOrResetYourPasswordButton)
    .expect(getLocation())
    .eql(LoginPage.path);
});
