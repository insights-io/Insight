import { v4 as uuid } from 'uuid';
import { appBaseUrl } from 'shared/config';
import {
  REBROWSE_ADMIN_DTO,
  sandbox,
  TOTP_SETUP_QR_IMAGE,
} from '@rebrowse/testing';
import { client } from 'sdk';
import { UserDTO } from '@rebrowse/types';

import { AbstractMfaChallengeTestPage } from './AbstractMfaChallengeTestPage';

export class SignInMfaChallengeEnforcedTestPage extends AbstractMfaChallengeTestPage {
  public static completeChallengeStub = ({
    redirect = appBaseUrl,
    sessionId = uuid(),
  }: {
    sessionId?: string;
    redirect?: string;
  } = {}) => {
    return sandbox
      .stub(client.accounts, 'completeEnforcedMfaChallenge')
      .callsFake(() =>
        AbstractMfaChallengeTestPage.completeChallengeMockImpl({
          redirect,
          sessionId,
        })
      );
  };

  public static updatePhoneNumberStub = ({
    user = REBROWSE_ADMIN_DTO,
  }: { user?: UserDTO } = {}) => {
    return sandbox
      .stub(client.users.phoneNumber, 'update')
      .callsFake((params) => {
        return Promise.resolve({
          statusCode: 200,
          headers: new Headers(),
          data: {
            ...user,
            phoneNumber: !params || !params.digits ? undefined : params,
          },
        });
      });
  };

  public static retrieveQrCodeStub = ({
    qrImage = TOTP_SETUP_QR_IMAGE,
  }: {
    qrImage?: string;
  } = {}) => {
    return sandbox.stub(client.accounts, 'startTotpSetup').resolves({
      statusCode: 200,
      headers: new Headers(),
      data: { qrImage },
    });
  };

  public static sendSmsCodeStub = () => {
    return sandbox.stub(client.accounts, 'sendSmsCode').resolves({
      statusCode: 200,
      headers: new Headers(),
      data: { validitySeconds: 60 },
    });
  };
}

export default new SignInMfaChallengeEnforcedTestPage({
  subheading: 'To secure your account, please setup Two-factor authentication.',
});
