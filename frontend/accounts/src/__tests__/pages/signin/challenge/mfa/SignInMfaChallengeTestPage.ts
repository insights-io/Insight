import { v4 as uuid } from 'uuid';
import { sandbox } from '@rebrowse/testing';
import { client } from 'sdk';
import { appBaseUrl } from 'shared/config';

import { AbstractMfaChallengeTestPage } from './AbstractMfaChallengeTestPage';

export class SignInMfaChallengeTestPage extends AbstractMfaChallengeTestPage {
  public static completeMfaChallengeStub = ({
    redirect = appBaseUrl,
    sessionId = uuid(),
  }: {
    sessionId?: string;
    redirect?: string;
  } = {}) => {
    return sandbox.stub(client.accounts, 'completeMfaChallenge').callsFake(() =>
      AbstractMfaChallengeTestPage.completeChallengeMockImpl({
        redirect,
        sessionId,
      })
    );
  };
}

export default new SignInMfaChallengeTestPage({
  subheading:
    'To secure your account, please complete the following verification.',
});
