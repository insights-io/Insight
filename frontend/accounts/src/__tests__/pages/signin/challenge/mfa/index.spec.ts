import {
  PWD_CHALLENGE_SESSION_ID,
  SIGNIN_MFA_CHALLENGE_ROUTE,
} from 'shared/constants/routes';
import { getPage } from '__tests__/utils';
import { v4 as uuid } from 'uuid';
import { appBaseUrl } from 'shared/config';
import {
  REBROWSE_ADMIN_NO_PHONE_NUMBER_DTO,
  sandbox,
  typePinCode,
} from '@rebrowse/testing';
import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/react';
import * as windowUtils from 'shared/utils/window';
import { INCLUDE_CREDENTIALS } from 'sdk';
import type { AutoSizerProps } from 'react-virtualized-auto-sizer';

import * as SignInPageSetup from '../../SignInPageSetup';
import signInPwdChallangeTestPage from '../pwd/SignInPwdChallengeTestPage';

import signInMfaChallengeTestPage, {
  SignInMfaChallengeTestPage,
} from './SignInMfaChallengeTestPage';
import signInMfaChallengeEnforcedTestPage, {
  SignInMfaChallengeEnforcedTestPage,
} from './SignInMfaChallengeEnforcedTestPage';

// baseui country list
jest.mock('react-virtualized', () => {
  return {
    __esModule: true,
    ...jest.requireActual<Record<string, unknown>>('react-virtualized'),
    AutoSizer: ({ children }: AutoSizerProps) => {
      return children({ width: 500, height: 500 });
    },
  };
});

describe('/signin/challenge/mfa', () => {
  const email = 'john.doe@gmail.com';
  const redirect = appBaseUrl;
  const code = 123456;
  const digits = '51222333';

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
    await signInPwdChallangeTestPage.findElements(email);
  });

  describe('TOTP', () => {
    test('As a user I can complete MFA challenge', async () => {
      const locationAssignStub = sandbox.stub(windowUtils, 'locationAssign');
      const retrieveChallengeStub = SignInMfaChallengeTestPage.retrieveMfaChallengeStub();
      const completeChallengeStub = SignInMfaChallengeTestPage.completeMfaChallengeStub();

      const {
        challengeId,
        continueButton,
        googleAuthenticatorTab,
        codeInput,
      } = await signInMfaChallengeTestPage.setup();
      sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId);

      userEvent.click(googleAuthenticatorTab);
      userEvent.click(continueButton);

      await screen.findByText('Required');

      typePinCode(codeInput, code);

      userEvent.click(continueButton);

      await waitFor(() => {
        sandbox.assert.calledWithExactly(locationAssignStub, redirect);
      });

      sandbox.assert.calledWithExactly(
        completeChallengeStub,
        { code, method: 'totp' },
        INCLUDE_CREDENTIALS
      );
    });

    test('As a user I can setup MFA if enforced', async () => {
      const locationAssignStub = sandbox.stub(windowUtils, 'locationAssign');
      const retrieveChallengeStub = SignInMfaChallengeEnforcedTestPage.retrieveMfaChallengeStub(
        { methods: [] }
      );
      const retrieveQrCodeStub = SignInMfaChallengeEnforcedTestPage.retrieveQrCodeStub();
      const completeChallengeStub = SignInMfaChallengeEnforcedTestPage.completeChallengeStub();

      const {
        challengeId,
        continueButton,
        codeInput,
      } = await signInMfaChallengeEnforcedTestPage.setup();

      sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId);
      sandbox.assert.calledWithExactly(retrieveQrCodeStub, INCLUDE_CREDENTIALS);
      userEvent.click(continueButton);
      await screen.findByText('Required');

      typePinCode(codeInput, code);
      userEvent.click(continueButton);

      await waitFor(() => {
        sandbox.assert.calledWithExactly(locationAssignStub, redirect);
      });

      sandbox.assert.calledWithExactly(
        completeChallengeStub,
        { code, method: 'totp' },
        INCLUDE_CREDENTIALS
      );
    });
  });

  describe('SMS', () => {
    test('As a user I can complete MFA challenge', async () => {
      const locationAssignStub = sandbox.stub(windowUtils, 'locationAssign');
      const retrieveChallengeStub = SignInMfaChallengeTestPage.retrieveMfaChallengeStub();
      const completeChallengeStub = SignInMfaChallengeTestPage.completeMfaChallengeStub();

      const {
        challengeId,
        continueButton,
        smsTab,
        codeInput,
      } = await signInMfaChallengeTestPage.setup();
      sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId);

      userEvent.click(smsTab);
      userEvent.click(continueButton);

      await screen.findByText('Required');

      typePinCode(codeInput, code);

      userEvent.click(continueButton);

      await waitFor(() => {
        sandbox.assert.calledWithExactly(locationAssignStub, redirect);
      });

      sandbox.assert.calledWithExactly(
        completeChallengeStub,
        { code, method: 'sms' },
        INCLUDE_CREDENTIALS
      );
    });

    test('As a user I can setup MFA if enforced (existing phone number)', async () => {
      const locationAssignStub = sandbox.stub(windowUtils, 'locationAssign');
      const retrieveChallengeStub = SignInMfaChallengeEnforcedTestPage.retrieveMfaChallengeStub(
        { methods: [] }
      );
      const retrieveQrCodeStub = SignInMfaChallengeEnforcedTestPage.retrieveQrCodeStub();
      const sendSmsCodeStub = SignInMfaChallengeEnforcedTestPage.sendSmsCodeStub();
      const completeChallengeStub = SignInMfaChallengeEnforcedTestPage.completeChallengeStub();

      const {
        smsTab,
        challengeId,
      } = await signInMfaChallengeEnforcedTestPage.setup();

      sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId);
      sandbox.assert.calledWithExactly(retrieveQrCodeStub, INCLUDE_CREDENTIALS);

      userEvent.click(smsTab);

      const {
        continueButton,
        codeInput,
      } = signInMfaChallengeEnforcedTestPage.getElements();

      userEvent.click(
        screen.getByRole(
          ...signInMfaChallengeEnforcedTestPage.matchers.sendSmsCode
        )
      );

      await screen.findByText('Code sent');
      expect(screen.getByText('60s')).toBeInTheDocument();
      sandbox.assert.calledWithExactly(sendSmsCodeStub, INCLUDE_CREDENTIALS);

      typePinCode(codeInput, code);
      userEvent.click(continueButton);

      await waitFor(() => {
        sandbox.assert.calledWithExactly(locationAssignStub, redirect);
      });

      sandbox.assert.calledWithExactly(
        completeChallengeStub,
        { code, method: 'sms' },
        INCLUDE_CREDENTIALS
      );
    });

    test('As a user I can setup MFA if enforced (new phone number)', async () => {
      const locationAssignStub = sandbox.stub(windowUtils, 'locationAssign');
      const retrieveChallengeStub = SignInMfaChallengeEnforcedTestPage.retrieveMfaChallengeStub(
        { methods: [], user: REBROWSE_ADMIN_NO_PHONE_NUMBER_DTO }
      );
      const retrieveQrCodeStub = SignInMfaChallengeEnforcedTestPage.retrieveQrCodeStub();
      const sendSmsCodeStub = SignInMfaChallengeEnforcedTestPage.sendSmsCodeStub();
      const completeChallengeStub = SignInMfaChallengeEnforcedTestPage.completeChallengeStub();
      const updatePhoneNumberStub = SignInMfaChallengeEnforcedTestPage.updatePhoneNumberStub();

      const {
        challengeId,
        smsTab,
      } = await signInMfaChallengeEnforcedTestPage.setup();

      sandbox.assert.calledWithExactly(retrieveChallengeStub, challengeId);
      sandbox.assert.calledWithExactly(retrieveQrCodeStub, INCLUDE_CREDENTIALS);

      userEvent.click(smsTab);

      const countryInput = screen.getByRole(
        ...signInMfaChallengeEnforcedTestPage.matchers.countryInput
      );
      userEvent.click(countryInput);
      userEvent.type(countryInput, 'Slove');
      userEvent.click(screen.getByText('Slovenia (Slovenija)'));

      const phoneNumberInput = screen.getByPlaceholderText(
        ...signInMfaChallengeEnforcedTestPage.matchers.phoneNumberInput
      );

      userEvent.type(phoneNumberInput, digits);
      userEvent.click(
        screen.getByRole(
          ...signInMfaChallengeEnforcedTestPage.matchers.continueButton
        )
      );

      const {
        codeInput,
        continueButton,
      } = await signInMfaChallengeEnforcedTestPage.findElements();

      sandbox.assert.calledWithExactly(
        updatePhoneNumberStub,
        { countryCode: '+386', digits },
        INCLUDE_CREDENTIALS
      );

      userEvent.click(
        screen.getByRole(
          ...signInMfaChallengeEnforcedTestPage.matchers.sendSmsCode
        )
      );

      await screen.findByText('Code sent');
      expect(screen.getByText('60s')).toBeInTheDocument();
      sandbox.assert.calledWithExactly(sendSmsCodeStub, INCLUDE_CREDENTIALS);

      typePinCode(codeInput, code);
      userEvent.click(continueButton);

      await waitFor(() => {
        sandbox.assert.calledWithExactly(locationAssignStub, redirect);
      });

      sandbox.assert.calledWithExactly(
        completeChallengeStub,
        { code, method: 'sms' },
        INCLUDE_CREDENTIALS
      );
    });
  });
});
