import {
  PWD_CHALLENGE_SESSION_ID,
  SIGNIN_MFA_CHALLENGE_ROUTE,
} from 'shared/constants/routes';
import { getPage } from '__tests__/utils';
import { v4 as uuid } from 'uuid';
import { appBaseUrl } from 'shared/config';
import { sandbox } from '@rebrowse/testing';
import userEvent from '@testing-library/user-event';

import * as SignInPageSetup from '../../SignInPageSetup';
import * as SignInPwdChallengeTestSetup from '../pwd/SignInPwdChallengeTestSetup';

import * as SignInMfaChallengeTestSetup from './SignInMfaChallengeTestSetup';

describe('/signin/challenge/mfa', () => {
  const email = 'john.doe@gmail.com';
  const redirect = appBaseUrl;

  test('As a user I should be redirected back to sign in page when mfa session missing', async () => {
    const { render } = await getPage({ route: SIGNIN_MFA_CHALLENGE_ROUTE });
    render();
    await SignInPageSetup.findElements();
  });

  test('As a user I should be redirected back to pwd challenge page when mfa session missing but pwd challenge exists', async () => {
    const challengeId = uuid();
    document.cookie = `${PWD_CHALLENGE_SESSION_ID}=${challengeId}`;
    const retrieveChallengeStub = SignInPageSetup.retrievePasswordChallengeStub(
      { email, redirect }
    );
    const { render } = await getPage({ route: SIGNIN_MFA_CHALLENGE_ROUTE });
    sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId);
    render();
    await SignInPwdChallengeTestSetup.findElements(email);
  });

  test('As a user I can complete totp challenge', async () => {
    const retrieveChallengeStub = SignInMfaChallengeTestSetup.retrieveMfaChallengeStub(
      ['totp', 'sms']
    );
    const {
      challengeId,
      continueButton,
    } = await SignInMfaChallengeTestSetup.setup({});
    sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId);
    userEvent.click(continueButton);
  });
});
