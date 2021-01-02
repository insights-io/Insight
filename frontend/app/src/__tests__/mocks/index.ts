/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  ADMIN_TEAM_INVITE_DTO,
  REBROWSE_EVENTS,
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_SESSION_INFO,
  SMS_MFA_SETUP_DTO,
  TOTP_MFA_SETUP_DTO,
  TOTP_MFA_SETUP_QR_IMAGE,
} from '__tests__/data';
import { v4 as uuid } from 'uuid';
import type {
  AuthTokenDTO,
  BrowserEventDTO,
  InvoiceDTO,
  MfaMethod,
  MfaSetupDTO,
  OrganizationPasswordPolicyDTO,
  PlanDTO,
  SamlConfigurationDTO,
  SamlSsoMethod,
  SessionDTO,
  SessionInfoDTO,
  SsoSetupDTO,
  SubscriptionDTO,
  TeamInviteDTO,
  UserDTO,
} from '@rebrowse/types';
import type { SinonSandbox } from 'sinon';
import { httpOkResponse, jsonPromise } from '__tests__/utils/request';
import { BOOTSTRAP_SCRIPT } from '__tests__/data/recording';
import { mockApiError } from '@rebrowse/storybook';
import { AUTH_TOKEN_DTO } from '__tests__/data/sso';
import {
  ACTIVE_BUSINESS_SUBSCRIPTION_DTO,
  ACTIVE_BUSINESS_SUBSCRIPTION_PAID_INVOICE_DTO,
  REBROWSE_PLAN_DTO,
} from '__tests__/data/billing';
import { addDays } from 'date-fns';
import { client } from 'sdk';

import {
  filterSession,
  countSessionsBy,
  retrieveSessionMockImplementation,
  searchEventsMockImplementation,
  getDistinctMockImplementation,
} from './filter';
import {
  countTeamInvitesMockImplementation,
  countUsersMockImplementation,
  searchTeamInvitesMockImplementation,
  searchUsersMockImplementation,
} from './filter/users';

export const mockAuth = (
  sandbox: SinonSandbox,
  data: SessionInfoDTO = REBROWSE_SESSION_INFO
) => {
  const sessionInfo = data;

  const retrieveSessionStub = sandbox
    .stub(client.auth.sso.sessions, 'retrieve')
    .callsFake(() => jsonPromise({ status: 200, data: sessionInfo }));

  const retrieveUserStub = sandbox
    .stub(client.auth.users, 'me')
    .callsFake(() => Promise.resolve(httpOkResponse(sessionInfo.user)));

  const retrieveOrganizationStub = sandbox
    .stub(client.auth.organizations, 'get')
    .callsFake(() => Promise.resolve(httpOkResponse(data.organization)));

  const updateOrganizationStub = sandbox
    .stub(client.auth.organizations, 'update')
    .callsFake((params) => {
      sessionInfo.organization = { ...sessionInfo.organization, ...params };
      return Promise.resolve(httpOkResponse(sessionInfo.organization));
    });

  const updateUserStub = sandbox
    .stub(client.auth.users, 'update')
    .callsFake((params) => {
      sessionInfo.user = { ...sessionInfo.user, ...(params as UserDTO) };
      return Promise.resolve(httpOkResponse(sessionInfo.user));
    });

  const updatePhoneNumberStub = sandbox
    .stub(client.auth.users.phoneNumber, 'update')
    .callsFake((params) => {
      sessionInfo.user.phoneNumber = params === null ? undefined : params;
      return Promise.resolve(httpOkResponse(sessionInfo.user));
    });

  return {
    retrieveSessionStub,
    retrieveUserStub,
    retrieveOrganizationStub,
    updateOrganizationStub,
    updateUserStub,
    updatePhoneNumberStub,
  };
};

export const mockOrganizationSettingsUsageAndPaymentsPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
    invoices = [ACTIVE_BUSINESS_SUBSCRIPTION_PAID_INVOICE_DTO],
  }: { sessionInfo?: SessionInfoDTO; invoices?: InvoiceDTO[] } = {}
) => {
  const authMocks = mockAuth(sandbox, sessionInfo);

  const listInvoicesStub = sandbox
    .stub(client.billing.invoices, 'list')
    .resolves(httpOkResponse(invoices));

  return { ...authMocks, listInvoicesStub };
};

export const mockOrganizationSettingsMemberInvitesPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
    invites: initialInvites = [],
  }: { sessionInfo?: SessionInfoDTO; invites?: TeamInviteDTO[] } = {}
) => {
  let invites = initialInvites;
  const authMocks = mockAuth(sandbox, sessionInfo);

  const listTeamInvitesStub = sandbox
    .stub(client.auth.organizations.teamInvite, 'list')
    .callsFake((args = {}) =>
      searchTeamInvitesMockImplementation(args.search, invites)
    );

  const countTeamInvitesStub = sandbox
    .stub(client.auth.organizations.teamInvite, 'count')
    .callsFake((args = {}) =>
      countTeamInvitesMockImplementation(args.search, invites)
    );

  const createTeamInviteStub = sandbox
    .stub(client.auth.organizations.teamInvite, 'create')
    .callsFake((createTeamInviteParams) => {
      const newTeamInvite: TeamInviteDTO = {
        ...createTeamInviteParams,
        creator: sessionInfo.user.id,
        createdAt: new Date().toISOString(),
        expiresAt: addDays(new Date(), 1).toISOString(),
        organizationId: sessionInfo.organization.id,
        valid: true,
        token: uuid(),
      };
      invites = [...invites, newTeamInvite];
      return Promise.resolve(httpOkResponse(newTeamInvite));
    });

  return {
    ...authMocks,
    listTeamInvitesStub,
    countTeamInvitesStub,
    createTeamInviteStub,
  };
};

export const mockOrganizationSettingsSubscriptionDetailsPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
    subscription: initialSubscription = ACTIVE_BUSINESS_SUBSCRIPTION_DTO,
    invoices = [ACTIVE_BUSINESS_SUBSCRIPTION_PAID_INVOICE_DTO],
  }: {
    sessionInfo?: SessionInfoDTO;
    subscription?: SubscriptionDTO;
    invoices?: InvoiceDTO[];
  } = {}
) => {
  const authMocks = mockAuth(sandbox, sessionInfo);
  let subscription = initialSubscription;

  const retrieveSubscriptionStub = sandbox
    .stub(client.billing.subscriptions, 'retrieve')
    .callsFake(() => Promise.resolve(httpOkResponse(subscription)));

  const listInvoicesStub = sandbox
    .stub(client.billing.invoices, 'listBySubscription')
    .resolves(httpOkResponse(invoices));

  const cancelSubscriptionStub = sandbox
    .stub(client.billing.subscriptions, 'cancel')
    .callsFake(() => {
      subscription = { ...subscription, status: 'canceled' };
      return Promise.resolve(httpOkResponse(subscription));
    });

  return {
    ...authMocks,
    retrieveSubscriptionStub,
    listInvoicesStub,
    cancelSubscriptionStub,
  };
};

export const mockOrganizationSettingsSubscriptionPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
    plan = REBROWSE_PLAN_DTO,
  }: { sessionInfo?: SessionInfoDTO; plan?: PlanDTO } = {}
) => {
  const authMocks = mockAuth(sandbox, sessionInfo);

  const retrieveActivePlanStub = sandbox
    .stub(client.billing.subscriptions, 'retrieveActivePlan')
    .resolves(httpOkResponse(plan));

  const listSubscriptionsStub = sandbox
    .stub(client.billing.subscriptions, 'list')
    .resolves(httpOkResponse([]));

  return { ...authMocks, retrieveActivePlanStub, listSubscriptionsStub };
};

export const mockOrganizationSettingsSecurityPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
    passwordPolicy: initialPasswordPolicy,
  }: {
    sessionInfo?: SessionInfoDTO;
    passwordPolicy?: OrganizationPasswordPolicyDTO;
  } = {}
) => {
  let passwordPolicy = initialPasswordPolicy;
  const authMocks = mockAuth(sandbox, sessionInfo);

  const retrievePasswordPolicyStub = sandbox
    .stub(client.auth.organizations.passwordPolicy, 'retrieve')
    .callsFake(() =>
      !passwordPolicy
        ? Promise.reject(
            mockApiError({
              statusCode: 404,
              message: 'Not Found',
              reason: 'Not Found',
            })
          )
        : Promise.resolve(httpOkResponse(passwordPolicy))
    );

  const updatePasswordPolicyStub = sandbox
    .stub(client.auth.organizations.passwordPolicy, 'update')
    .callsFake((updateParams) => {
      passwordPolicy = {
        ...(passwordPolicy as OrganizationPasswordPolicyDTO),
        ...updateParams,
      };
      return Promise.resolve(httpOkResponse(passwordPolicy));
    });

  const createPasswordPolicyStub = sandbox
    .stub(client.auth.organizations.passwordPolicy, 'create')
    .callsFake((createPasswordpolicyParams) => {
      const newPasswordPolicy = !passwordPolicy
        ? {
            ...createPasswordpolicyParams,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            organizationId: sessionInfo.organization.id,
          }
        : { ...passwordPolicy, ...createPasswordpolicyParams };

      passwordPolicy = newPasswordPolicy;
      return Promise.resolve(httpOkResponse(newPasswordPolicy));
    });

  return {
    ...authMocks,
    retrievePasswordPolicyStub,
    createPasswordPolicyStub,
    updatePasswordPolicyStub,
  };
};

export const mockOrganizationSettingsMembersPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
    members = [sessionInfo.user],
  }: { sessionInfo?: SessionInfoDTO; members?: UserDTO[] } = {}
) => {
  const users = members;

  const listMembersStub = sandbox
    .stub(client.auth.organizations.members, 'list')
    .callsFake((args = {}) =>
      searchUsersMockImplementation(args.search, users)
    );

  const countMembersStub = sandbox
    .stub(client.auth.organizations.members, 'count')
    .callsFake((args = {}) => countUsersMockImplementation(args.search, users));

  const authMocks = mockAuth(sandbox, sessionInfo);

  return { ...authMocks, listMembersStub, countMembersStub };
};

export const mockOrganizationSettingsGeneralPage = (
  sandbox: SinonSandbox,
  { sessionInfo = REBROWSE_SESSION_INFO }: { sessionInfo?: SessionInfoDTO } = {}
) => {
  const authMocks = mockAuth(sandbox, sessionInfo);
  return authMocks;
};

export const mockOrganizationAuthPage = (
  sandbox: SinonSandbox,
  {
    ssoSetup,
    sessionInfo = REBROWSE_SESSION_INFO,
  }: {
    ssoSetup?: SsoSetupDTO;
    sessionInfo?: SessionInfoDTO;
  } = {}
) => {
  let actualSsoSetup = ssoSetup;

  const authMocks = mockAuth(sandbox, sessionInfo);

  const disableSsoSetupStub = sandbox
    .stub(client.auth.sso.setups, 'delete')
    .callsFake(() => {
      actualSsoSetup = undefined;
      return Promise.resolve({ statusCode: 200, headers: new Headers() });
    });

  const retrieveSsoSetupStub = sandbox
    .stub(client.auth.sso.setups, 'retrieve')
    .callsFake(() => {
      if (!actualSsoSetup) {
        return Promise.reject(
          mockApiError({
            statusCode: 404,
            message: 'Not Found',
            reason: 'Not Found',
          })
        );
      }
      return Promise.resolve(httpOkResponse(actualSsoSetup));
    });

  const createSsoSetupStub = sandbox
    .stub(client.auth.sso.setups, 'create')
    .callsFake((method, saml) => {
      actualSsoSetup = {
        method: method as SamlSsoMethod,
        saml: saml as SamlConfigurationDTO,
        organizationId: sessionInfo.user.organizationId,
        createdAt: new Date().toISOString(),
        domain: sessionInfo.user.email.split('@')[1],
      };
      return Promise.resolve(httpOkResponse(actualSsoSetup));
    });

  return {
    ...authMocks,
    retrieveSsoSetupStub,
    createSsoSetupStub,
    disableSsoSetupStub,
  };
};

export const mockAcocuntSettingsAuthTokensPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
    authTokens = [AUTH_TOKEN_DTO],
  }: {
    sessionInfo?: SessionInfoDTO;
    authTokens?: AuthTokenDTO[];
  } = {}
) => {
  let tokens = authTokens;
  const authMocks = mockAuth(sandbox, sessionInfo);

  const listAuthTokensStub = sandbox
    .stub(client.auth.tokens, 'list')
    .callsFake(() => Promise.resolve(httpOkResponse(tokens)));

  const deleteAuthTokenStub = sandbox
    .stub(client.auth.tokens, 'delete')
    .callsFake((token) => {
      tokens = tokens.filter((t) => t.token !== token);
      return Promise.resolve(httpOkResponse(true));
    });

  const createAuthTokensStub = sandbox
    .stub(client.auth.tokens, 'create')
    .callsFake(() => {
      const newAuthToken: AuthTokenDTO = {
        userId: sessionInfo.user.id,
        createdAt: new Date().toISOString(),
        token: uuid(),
      };
      tokens = [...tokens, newAuthToken];
      return Promise.resolve(httpOkResponse(newAuthToken));
    });

  return {
    ...authMocks,
    listAuthTokensStub,
    createAuthTokensStub,
    deleteAuthTokenStub,
  };
};

export const mockAccountSettingsDetailsPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
  }: {
    sessionInfo?: SessionInfoDTO;
  } = {}
) => {
  const authMocks = mockAuth(sandbox, sessionInfo);
  return { ...authMocks };
};

export const mockAccountSettingsSecurityPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
    mfaSetups = [TOTP_MFA_SETUP_DTO, SMS_MFA_SETUP_DTO],
  }: {
    sessionInfo?: SessionInfoDTO;
    mfaSetups?: MfaSetupDTO[];
  } = {}
) => {
  const authMocks = mockAuth(sandbox, sessionInfo);
  let setups = mfaSetups;

  const changePasswordStub = sandbox
    .stub(client.auth.password, 'change')
    .resolves({ statusCode: 200, headers: new Headers() });

  const listMfaSetupsStub = sandbox
    .stub(client.auth.mfa.setup, 'list')
    .callsFake(() => Promise.resolve(httpOkResponse(setups)));

  const startMfaTotpSetupStub = sandbox
    .stub(client.auth.mfa.setup.totp, 'start')
    .resolves(httpOkResponse({ qrImage: TOTP_MFA_SETUP_QR_IMAGE }));

  const completeMfaSetupStub = sandbox
    .stub(client.auth.mfa.setup, 'complete')
    .callsFake((method) => {
      const newSetup: MfaSetupDTO = {
        method,
        createdAt: new Date().toISOString(),
      };
      setups.push(newSetup);
      return Promise.resolve(httpOkResponse(newSetup));
    });

  const disableMfaSetupStub = sandbox
    .stub(client.auth.mfa.setup, 'disable')
    .callsFake((method) => {
      setups = setups.filter((s) => s.method !== method);
      return Promise.resolve({ statusCode: 200, headers: new Headers() });
    });

  return {
    ...authMocks,
    listMfaSetupsStub,
    startMfaTotpSetupStub,
    completeMfaSetupStub,
    disableMfaSetupStub,
    changePasswordStub,
  };
};

export const mockSessionPage = (
  sandbox: SinonSandbox,
  {
    sessions = REBROWSE_SESSIONS_DTOS,
    events = REBROWSE_EVENTS,
    sessionInfo = REBROWSE_SESSION_INFO,
  }: {
    sessions?: SessionDTO[];
    events?: BrowserEventDTO[];
    sessionInfo?: SessionInfoDTO;
  } = {}
) => {
  const authMocks = mockAuth(sandbox, sessionInfo);

  const retrieveSessionStub = sandbox
    .stub(client.recording.sessions, 'retrieve')
    .callsFake((id) => retrieveSessionMockImplementation(id, sessions));

  const searchEventsStub = sandbox
    .stub(client.recording.events, 'search')
    .callsFake((_, args = {}) =>
      searchEventsMockImplementation(events, args.search)
    );

  return { ...authMocks, retrieveSessionStub, searchEventsStub };
};

export const mockSessionsPage = (
  sandbox: SinonSandbox,
  {
    sessions = REBROWSE_SESSIONS_DTOS,
    sessionInfo = REBROWSE_SESSION_INFO,
  }: {
    sessions?: SessionDTO[];
    sessionInfo?: SessionInfoDTO;
  } = {}
) => {
  const sessionMocks = mockSessionPage(sandbox, { sessions, sessionInfo });

  const countSessionsStub = sandbox
    .stub(client.recording.sessions, 'count')
    .callsFake((args = {}) => {
      return Promise.resolve(
        httpOkResponse(countSessionsBy(sessions, args.search))
      );
    });

  const listSessionsStub = sandbox
    .stub(client.recording.sessions, 'list')
    .callsFake((args = {}) => {
      return Promise.resolve(
        httpOkResponse(
          sessions.filter((session) => filterSession(session, args.search))
        )
      );
    });

  const getDistinctSessionsStub = sandbox
    .stub(client.recording.sessions, 'distinct')
    .callsFake((on) => {
      return getDistinctMockImplementation(on);
    });

  return {
    ...sessionMocks,
    listSessionsStub,
    countSessionsStub,
    getDistinctSessionsStub,
  };
};

export const mockEmptySessionsPage = (sandbox: SinonSandbox) => {
  const mocks = mockSessionsPage(sandbox, { sessions: [] });
  const retrieveBoostrapScriptStub = sandbox
    .stub(client.tracking, 'retrieveBoostrapScript')
    .resolves({
      data: BOOTSTRAP_SCRIPT,
      statusCode: 200,
      headers: new Headers(),
    });

  return { ...mocks, retrieveBoostrapScriptStub };
};

export const mockIndexPage = (
  sandbox: SinonSandbox,
  {
    sessions = REBROWSE_SESSIONS_DTOS,
    sessionInfo = REBROWSE_SESSION_INFO,
  }: { sessions?: SessionDTO[]; sessionInfo?: SessionInfoDTO } = {}
) => {
  const authMocks = mockAuth(sandbox, sessionInfo);
  const countSessionsStub = sandbox
    .stub(client.recording.sessions, 'count')
    .callsFake((args = {}) => {
      return Promise.resolve(
        httpOkResponse(countSessionsBy(sessions, args.search))
      );
    });

  const countPageVisitsStub = sandbox
    .stub(client.recording.pageVisits, 'count')
    .callsFake((args = {}) => {
      return Promise.resolve(
        httpOkResponse(countSessionsBy(sessions, args.search as any)) as any
      );
    });

  return { ...authMocks, countPageVisitsStub, countSessionsStub };
};

export const mockLoginPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
    login = true,
    ssoSetupByDomain = false,
  }: {
    sessionInfo?: SessionInfoDTO;
    login?: boolean;
    ssoSetupByDomain?: string | false;
  } = {}
) => {
  const verificationPageMocks = mockVerificationPage(sandbox, { sessionInfo });

  const loginStub = sandbox
    .stub(client.auth.sso.sessions, 'login')
    .callsFake(() => {
      if (login) {
        document.cookie = 'SessionId=123';
      } else {
        document.cookie = 'ChallengeId=123';
      }
      return Promise.resolve(httpOkResponse(login));
    });

  const retrieveSsoSetupByDomainStub = sandbox
    .stub(client.auth.sso.setups, 'retrieveByDomain')
    .resolves(httpOkResponse(ssoSetupByDomain));

  return { ...verificationPageMocks, loginStub, retrieveSsoSetupByDomainStub };
};

export const mockVerificationPage = (
  sandbox: SinonSandbox,
  {
    methods = ['totp'],
    sessionInfo = REBROWSE_SESSION_INFO,
  }: {
    methods?: MfaMethod[];
    sessionInfo?: SessionInfoDTO;
  } = {}
) => {
  const indexPageMocks = mockIndexPage(sandbox, { sessionInfo });

  const retrieveChallengeStub = sandbox
    .stub(client.auth.mfa.challenge, 'retrieve')
    .resolves(httpOkResponse(methods));

  const completeChallengeStub = sandbox
    .stub(client.auth.mfa.challenge, 'complete')
    .callsFake(() => {
      document.cookie = 'SessionId=123';
      return Promise.resolve({ statusCode: 200, headers: new Headers() });
    });

  const retrieveUserByChallengeStub = sandbox
    .stub(client.auth.mfa.challenge, 'retrieveCurrentUser')
    .resolves(httpOkResponse(sessionInfo.user));

  const startTotpMfaSetupStub = sandbox
    .stub(client.auth.mfa.setup.totp, 'start')
    .resolves(httpOkResponse({ qrImage: TOTP_MFA_SETUP_QR_IMAGE }));

  const completeEnforcedMfaSetupStub = sandbox
    .stub(client.auth.mfa.setup, 'completeEnforced')
    .callsFake(() => {
      document.cookie = 'SessionId=123';
      return Promise.resolve(
        httpOkResponse({
          createdAt: new Date().toISOString(),
          method: 'totp',
        })
      );
    });

  const sendChallengeSmsCodeStub = sandbox
    .stub(client.auth.mfa.challenge, 'sendSmsCode')
    .resolves(httpOkResponse({ validitySeconds: 60 }));

  return {
    ...indexPageMocks,
    retrieveChallengeStub,
    completeChallengeStub,
    retrieveUserByChallengeStub,
    startTotpMfaSetupStub,
    completeEnforcedMfaSetupStub,
    sendChallengeSmsCodeStub,
  };
};

export const mockAcceptTeamInvitePage = (
  sandbox: SinonSandbox,
  {
    teamInvite = ADMIN_TEAM_INVITE_DTO,
    sessionInfo = REBROWSE_SESSION_INFO,
    sessionId = '123',
  }: {
    teamInvite?: TeamInviteDTO;
    sessionInfo?: SessionInfoDTO;
    sessionId?: string;
  } = {}
) => {
  const indexPageMocks = mockIndexPage(sandbox, { sessionInfo });

  const acceptTeamInviteStub = sandbox
    .stub(client.auth.organizations.teamInvite, 'accept')
    .callsFake(() => {
      document.cookie = `SessionId=${sessionId}`;
      return Promise.resolve({ statusCode: 200, headers: new Headers() });
    });

  const retrieveTeamInviteStub = sandbox
    .stub(client.auth.organizations.teamInvite, 'retrieve')
    .resolves(httpOkResponse(teamInvite));

  return { ...indexPageMocks, acceptTeamInviteStub, retrieveTeamInviteStub };
};

/* /password-reset */
export const mockPasswordResetPage = (
  sandbox: SinonSandbox,
  {
    exists = true,
    sessionInfo = REBROWSE_SESSION_INFO,
  }: { exists?: boolean; sessionInfo?: SessionInfoDTO } = {}
) => {
  const indexPageStubs = mockIndexPage(sandbox, { sessionInfo });

  const resetExistsStub = sandbox
    .stub(client.auth.password, 'resetExists')
    .resolves(httpOkResponse(exists));

  const passwordResetStub = sandbox
    .stub(client.auth.password, 'reset')
    .resolves({ statusCode: 200, headers: new Headers() });

  return { ...indexPageStubs, resetExistsStub, passwordResetStub };
};

/* /password-forgot */
export const mockPasswordForgotPage = (sandbox: SinonSandbox) => {
  const passwordForgotStub = sandbox
    .stub(client.auth.password, 'forgot')
    .resolves();

  return { passwordForgotStub };
};
