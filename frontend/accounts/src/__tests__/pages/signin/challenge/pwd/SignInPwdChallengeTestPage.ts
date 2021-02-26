import { getPage } from '__tests__/utils';
import { screen, waitFor } from '@testing-library/react';
import {
  PWD_CHALLENGE_SESSION_ID,
  SESSION_ID,
  SIGNIN_PWD_CHALLENGE_ROUTE,
  MFA_CHALLENGE_SESSION_ID,
} from 'shared/constants/routes';
import { appBaseUrl } from 'shared/config';
import { v4 as uuid } from 'uuid';
import { deleteCookie, REBROWSE_ADMIN_DTO, sandbox } from '@rebrowse/testing';
import { client } from 'sdk';
import { MfaMethod, UserDTO } from '@rebrowse/types';

import * as SignInPageSetup from '../../SignInPageSetup';

export class SignInPwdChallangeTestPage {
  public readonly matchers = {
    heading: ['heading', { name: "Verify it's you" }],
    accountSelector: (email: string) =>
      ['link', { name: new RegExp(email) }] as const,
    passwordInput: ['Password'],
    showPasswordButton: ['button', { name: 'Show password text' }],
    forgotPasswordLink: ['link', { name: 'Forgot?' }],
    continueButton: ['button', { name: 'Continue' }],
    createFreeAccountLink: ['link', { name: 'Create a free account' }],
    joinAnExistingTeamLink: ['link', { name: 'Join an existing team' }],
    signInWithGoogle: ['link', { name: /Sign in with Google/ }],
    signInWithGithub: ['link', { name: 'Sign in with Github' }],
    signInWithMicrosoft: ['link', { name: 'Sign in with Microsoft' }],
  } as const;

  public getElements = (email: string) => {
    return {
      heading: screen.getByRole(...this.matchers.heading),
      accountSelector: screen.getByRole(
        ...this.matchers.accountSelector(email)
      ),
      passwordInput: screen.getByPlaceholderText(
        ...this.matchers.passwordInput
      ),
      showPasswordButton: screen.getByRole(...this.matchers.showPasswordButton),
      forgotPasswordLink: screen.getByRole(...this.matchers.forgotPasswordLink),
      continueButton: screen.getByRole(...this.matchers.continueButton),
      createFreeAccountLink: screen.getByRole(
        ...this.matchers.createFreeAccountLink
      ),
      joinAnExistingTeamLink: screen.getByRole(
        ...this.matchers.joinAnExistingTeamLink
      ),
      signInWithGoogle: screen.getByRole(...this.matchers.signInWithGoogle),
      signInWithGithub: screen.getByRole(...this.matchers.signInWithGithub),
      signInWithMicrosoft: screen.getByRole(
        ...this.matchers.signInWithMicrosoft
      ),
    };
  };

  public findElements = (email: string) => {
    return waitFor(() => this.getElements(email));
  };

  public setup = async ({
    email,
    challengeId = uuid(),
    redirect = appBaseUrl,
    route = SIGNIN_PWD_CHALLENGE_ROUTE,
  }: {
    email: string;
    challengeId?: string;
    redirect?: string;
    route?: string;
  }) => {
    document.cookie = `${PWD_CHALLENGE_SESSION_ID}=${challengeId}`;
    const retrieveChallengeStub = SignInPageSetup.retrievePasswordChallengeStub(
      {
        email,
        redirect,
      }
    );
    const { render } = await getPage({ route });
    sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId);
    render();
    return { ...this.getElements(email), retrieveChallengeStub };
  };

  public static completePwdChallengeSuccess = ({
    location = appBaseUrl,
    sessionId = uuid(),
  }: {
    sessionId?: string;
    location?: string;
  } = {}) => {
    return sandbox
      .stub(client.accounts, 'completePwdChallenge')
      .callsFake(() => {
        document.cookie = `${SESSION_ID}=${sessionId}`;
        return Promise.resolve({
          statusCode: 200,
          headers: new Headers(),
          data: { action: 'SUCCESS', location },
        });
      });
  };

  public static completePwdChallengeMfa = ({
    challengeId = uuid(),
    methods = ['sms', 'totp'],
    user = REBROWSE_ADMIN_DTO,
  }: {
    challengeId?: string;
    methods?: MfaMethod[];
    user?: UserDTO;
  } = {}) => {
    const retrieveMfaChallengeStub = sandbox
      .stub(client.accounts, 'retrieveMfaChallenge')
      .resolves({
        data: { methods, user },
        statusCode: 200,
        headers: new Headers(),
      });

    const completePwdChallengeStub = sandbox
      .stub(client.accounts, 'completePwdChallenge')
      .callsFake(() => {
        document.cookie = `${MFA_CHALLENGE_SESSION_ID}=${challengeId}`;
        deleteCookie(PWD_CHALLENGE_SESSION_ID);
        return Promise.resolve({
          statusCode: 200,
          headers: new Headers(),
          data: { action: 'MFA_CHALLENGE', challengeId, methods },
        });
      });

    return { challengeId, retrieveMfaChallengeStub, completePwdChallengeStub };
  };
}

export default new SignInPwdChallangeTestPage();
