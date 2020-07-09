import { queryByText, getByText } from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import config from '../config';
import { getLocation } from '../utils';

fixture('/password-reset').page(
  `${config.appBaseURL}/password-reset?token=${uuid()}`
);

test('Should display nice message on password-reset with invalid token', async (t) => {
  await t
    .expect(
      queryByText(
        'It looks like this password reset request is invalid or has already been accepted.'
      ).visible
    )
    .ok('Should display nice message on password-reset with invalid token')
    .click(getByText('Log in or reset your password'))
    .expect(getLocation())
    .eql(`${config.appBaseURL}/login`);
});
