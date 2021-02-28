import userEvent from '@testing-library/user-event';
import { INCLUDE_CREDENTIALS } from 'sdk';
import { appBaseUrl } from 'shared/config';
import { sandbox } from '@rebrowse/testing';
import { v4 as uuid } from 'uuid';
import { waitFor } from '@testing-library/react';
import * as windowUtils from 'shared/utils/window';

import signInPwdChallengeTestSetup from './challenge/pwd/SignInPwdChallengeTestPage';
import * as SignInPageSetup from './SignInPageSetup';

const email = 'john.doe@gmail.com';
const challengeId = uuid();
const redirect = appBaseUrl;

describe('/signin', () => {
  test('As a user I need to verify its me with my password', async () => {
    const chooseAccountStub = SignInPageSetup.chooseAccountPwdChallengeStub(
      challengeId
    );
    const retrieveChallengeStub = SignInPageSetup.retrievePasswordChallengeStub(
      { email, redirect }
    );
    const { emailInput, continueButton } = await SignInPageSetup.setup();

    userEvent.type(emailInput, email);
    userEvent.click(continueButton);
    await signInPwdChallengeTestSetup.findElements(email);
    sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId);
    sandbox.assert.calledWithExactly(
      chooseAccountStub,
      { email, redirect },
      INCLUDE_CREDENTIALS
    );
  });

  test("As a user I'm redirected to SSO provider when SSO configured", async () => {
    const location =
      'https://accounts.google.com/o/oauth2/auth?client_id=237859759623-rfpiq8eo37afp0qc294ioqrjtq17q25h.apps.googleusercontent.com&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fv1%2Fsso%2Foauth2%2Fgoogle%2Fcallback&response_type=code&scope=openid+email+profile&state=89CTfkAo2EzqKN0MwnzGisqmkBhttp%3A%2F%2Flocalhost%3A3000';

    const chooseAccountStub = SignInPageSetup.chooseAccountSsoRedirectStub(
      location
    );

    const assignStub = sandbox.stub(windowUtils, 'locationAssign');
    const { emailInput, continueButton } = await SignInPageSetup.setup();

    userEvent.type(emailInput, email);
    userEvent.click(continueButton);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(assignStub, location);
    });

    sandbox.assert.calledWithExactly(
      chooseAccountStub,
      { email, redirect },
      INCLUDE_CREDENTIALS
    );
  });

  describe('As a user I want the page to be accessible', () => {
    test('Navigation via tabbing', async () => {
      /* Render */
      const {
        emailInput,
        continueButton,
        createFreeAcountLink,
        joinAnExistingTeamLink,
        signInWithGoogle,
        signInWithGithub,
        signInWithMicrosoft,
      } = await SignInPageSetup.setup();

      expect(document.activeElement).toEqual(emailInput);
      userEvent.tab();
      expect(document.activeElement).toEqual(continueButton);
      userEvent.tab();
      expect(document.activeElement).toEqual(createFreeAcountLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(joinAnExistingTeamLink);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithGoogle);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithGithub);
      userEvent.tab();
      expect(document.activeElement).toEqual(signInWithMicrosoft);
      userEvent.tab();
      expect(document.activeElement).toEqual(document.body);
      userEvent.tab();
      expect(document.activeElement).toEqual(emailInput);
    });
  });
});
