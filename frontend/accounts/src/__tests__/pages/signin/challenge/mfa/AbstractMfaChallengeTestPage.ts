import { deleteCookie, REBROWSE_ADMIN_DTO, sandbox } from '@rebrowse/testing';
import type { MfaMethod, UserDTO } from '@rebrowse/types';
import { screen, waitFor } from '@testing-library/react';
import { client } from 'sdk';
import {
  MFA_CHALLENGE_SESSION_ID,
  SESSION_ID,
  SIGNIN_MFA_CHALLENGE_ROUTE,
} from 'shared/constants/routes';
import { v4 as uuid } from 'uuid';
import { getPage } from '__tests__/utils';

export abstract class AbstractMfaChallengeTestPage {
  public matchers: ReturnType<
    typeof AbstractMfaChallengeTestPage['createMatchers']
  >;

  constructor({ subheading }: { subheading: string }) {
    this.matchers = AbstractMfaChallengeTestPage.createMatchers({ subheading });
  }

  static createMatchers = ({ subheading }: { subheading: string }) => {
    return {
      heading: ['heading', { name: 'Security verification' }],
      subheading: [
        'heading',
        {
          name: subheading,
        },
      ],
      googleAuthenticatorTab: ['tab', { name: 'Google Authenticator' }],
      smsTab: ['tab', { name: 'Text message' }],
      codeInput: ['Please enter your pin code'],
      continueButton: ['button', { name: 'Continue' }],
      qrCodeImage: ['Time-based one-time password QR code'],
      sendSmsCode: ['button', { name: 'Send Code' }],
      countryInput: ['combobox', { name: 'Select country' }],
      phoneNumberInput: ['51111222'],
    } as const;
  };

  public getElements = () => {
    return {
      heading: screen.getByRole(...this.matchers.heading),
      subheading: screen.getByRole(...this.matchers.subheading),
      googleAuthenticatorTab: screen.getByRole(
        ...this.matchers.googleAuthenticatorTab
      ),
      smsTab: screen.getByRole(...this.matchers.smsTab),
      codeInput: screen.getAllByLabelText(
        ...this.matchers.codeInput
      ) as HTMLInputElement[],
      continueButton: screen.getByRole(...this.matchers.continueButton),
    };
  };

  public findElements = () => {
    return waitFor(() => this.getElements());
  };

  public setup = async ({
    challengeId = uuid(),
    route = SIGNIN_MFA_CHALLENGE_ROUTE,
  }: {
    challengeId?: string;
    route?: string;
  } = {}) => {
    document.cookie = `${MFA_CHALLENGE_SESSION_ID}=${challengeId}`;
    const { render } = await getPage({ route });
    render();
    const elements = await this.findElements();
    return { ...elements, challengeId };
  };

  public static retrieveMfaChallengeStub = ({
    methods = ['sms', 'totp'],
    user = REBROWSE_ADMIN_DTO,
  }: {
    methods?: MfaMethod[];
    user?: UserDTO;
  } = {}) => {
    return sandbox.stub(client.accounts, 'retrieveMfaChallenge').resolves({
      statusCode: 200,
      headers: new Headers(),
      data: { methods, user },
    });
  };

  protected static completeChallengeMockImpl = ({
    sessionId,
    redirect,
  }: {
    sessionId: string;
    redirect: string;
  }) => {
    document.cookie = `${SESSION_ID}=${sessionId}`;
    deleteCookie(MFA_CHALLENGE_SESSION_ID);

    return Promise.resolve({
      statusCode: 200,
      headers: new Headers(),
      data: { action: 'SUCCESS', location: redirect },
    } as const);
  };
}
