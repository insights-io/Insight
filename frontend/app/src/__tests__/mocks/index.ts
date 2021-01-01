/* eslint-disable @typescript-eslint/no-explicit-any */
import { AuthApi, BillingApi, SessionApi } from 'api';
import {
  REBROWSE_EVENTS,
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_SESSION_INFO,
  SMS_MFA_SETUP_DTO,
  TOTP_MFA_SETUP_DTO,
  TOTP_MFA_SETUP_QR_IMAGE,
} from '__tests__/data';
import * as sdk from '@rebrowse/sdk';
import { v4 as uuid } from 'uuid';
import type {
  AuthTokenDTO,
  BrowserEventDTO,
  InvoiceDTO,
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
    .stub(AuthApi.sso.session, 'get')
    .callsFake(() => jsonPromise({ status: 200, data: sessionInfo }));

  const retrieveUserStub = sandbox
    .stub(AuthApi.user, 'me')
    .callsFake(() => Promise.resolve(httpOkResponse(sessionInfo.user)));

  const retrieveOrganizationStub = sandbox
    .stub(AuthApi.organization, 'get')
    .callsFake(() => Promise.resolve(httpOkResponse(data.organization)));

  const updateOrganizationStub = sandbox
    .stub(AuthApi.organization, 'update')
    .callsFake((params) => {
      sessionInfo.organization = { ...sessionInfo.organization, ...params };
      return Promise.resolve(httpOkResponse(sessionInfo.organization));
    });

  const updateUserStub = sandbox
    .stub(AuthApi.user, 'update')
    .callsFake((params) => {
      sessionInfo.user = { ...sessionInfo.user, ...(params as UserDTO) };
      return Promise.resolve(httpOkResponse(sessionInfo.user));
    });

  const updatePhoneNumberStub = sandbox
    .stub(AuthApi.user, 'updatePhoneNumber')
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

export const mockOrganizationSettingsMemberInvitesPage = (
  sandbox: SinonSandbox,
  {
    sessionInfo = REBROWSE_SESSION_INFO,
    invites: initialInvites = [],
  }: { sessionInfo?: SessionInfoDTO; invites?: TeamInviteDTO[] } = {}
) => {
  const invites = initialInvites;
  const authMocks = mockAuth(sandbox, sessionInfo);

  const listTeamInvitesStub = sandbox
    .stub(AuthApi.organization.teamInvite, 'list')
    .callsFake((args = {}) =>
      searchTeamInvitesMockImplementation(args.search, invites)
    );

  const countTeamInvitesStub = sandbox
    .stub(AuthApi.organization.teamInvite, 'count')
    .callsFake((args = {}) =>
      countTeamInvitesMockImplementation(args.search, invites)
    );

  return { ...authMocks, listTeamInvitesStub, countTeamInvitesStub };
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
    .stub(BillingApi.subscriptions, 'get')
    .callsFake(() => Promise.resolve(httpOkResponse(subscription)));

  const listInvoicesStub = sandbox
    .stub(BillingApi.invoices, 'listBySubscription')
    .resolves(httpOkResponse(invoices));

  const cancelSubscriptionStub = sandbox
    .stub(BillingApi.subscriptions, 'cancel')
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
    .stub(BillingApi.subscriptions, 'getActivePlan')
    .resolves(httpOkResponse(plan));

  const listSubscriptionsStub = sandbox
    .stub(BillingApi.subscriptions, 'list')
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
    .stub(AuthApi.organization.passwordPolicy, 'retrieve')
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
    .stub(AuthApi.organization.passwordPolicy, 'update')
    .callsFake((updateParams) => {
      passwordPolicy = {
        ...(passwordPolicy as OrganizationPasswordPolicyDTO),
        ...updateParams,
      };
      return Promise.resolve(httpOkResponse(passwordPolicy));
    });

  const createPasswordPolicyStub = sandbox
    .stub(AuthApi.organization.passwordPolicy, 'create')
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
    .stub(AuthApi.organization.members, 'list')
    .callsFake((args = {}) =>
      searchUsersMockImplementation(args.search, users)
    );

  const countMembersStub = sandbox
    .stub(AuthApi.organization.members, 'count')
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
    .stub(AuthApi.sso.setup, 'delete')
    .callsFake(() => {
      actualSsoSetup = undefined;
      return Promise.resolve({ statusCode: 200, headers: new Headers() });
    });

  const retrieveSsoSetupStub = sandbox
    .stub(AuthApi.sso.setup, 'get')
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
    .stub(AuthApi.sso.setup, 'create')
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
    .stub(AuthApi.sso.token, 'list')
    .callsFake(() => Promise.resolve(httpOkResponse(tokens)));

  const deleteAuthTokenStub = sandbox
    .stub(AuthApi.sso.token, 'delete')
    .callsFake((token) => {
      tokens = tokens.filter((t) => t.token !== token);
      return Promise.resolve(httpOkResponse(true));
    });

  const createAuthTokensStub = sandbox
    .stub(AuthApi.sso.token, 'create')
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
    .stub(AuthApi.password, 'change')
    .resolves({ statusCode: 200, headers: new Headers() });

  const listMfaSetupsStub = sandbox
    .stub(AuthApi.mfa.setup, 'list')
    .callsFake(() => Promise.resolve(httpOkResponse(setups)));

  const startMfaTotpSetupStub = sandbox
    .stub(AuthApi.mfa.setup.totp, 'start')
    .resolves(httpOkResponse({ qrImage: TOTP_MFA_SETUP_QR_IMAGE }));

  const completeMfaSetupStub = sandbox
    .stub(AuthApi.mfa.setup, 'complete')
    .callsFake((method) => {
      const newSetup: MfaSetupDTO = {
        method,
        createdAt: new Date().toISOString(),
      };
      setups.push(newSetup);
      return Promise.resolve(httpOkResponse(newSetup));
    });

  const disableMfaSetupStub = sandbox
    .stub(AuthApi.mfa.setup, 'disable')
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
    .stub(SessionApi, 'getSession')
    .callsFake((id) => retrieveSessionMockImplementation(id, sessions));

  const searchEventsStub = sandbox
    .stub(SessionApi.events, 'search')
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
    .stub(SessionApi, 'count')
    .callsFake((args = {}) => {
      return Promise.resolve(
        httpOkResponse(countSessionsBy(sessions, args.search))
      );
    });

  const listSessionsStub = sandbox
    .stub(SessionApi, 'getSessions')
    .callsFake((args = {}) => {
      return Promise.resolve(
        httpOkResponse(
          sessions.filter((session) => filterSession(session, args.search))
        )
      );
    });

  const getDistinctStub = sandbox
    .stub(SessionApi, 'distinct')
    .callsFake((on) => {
      return getDistinctMockImplementation(on);
    });

  return {
    ...sessionMocks,
    listSessionsStub,
    countSessionsStub,
    getDistinctStub,
  };
};

export const mockEmptySessionsPage = (sandbox: SinonSandbox) => {
  const mocks = mockSessionsPage(sandbox, { sessions: [] });
  const retrieveBoostrapScriptStub = sandbox
    .stub(sdk, 'getBoostrapScript')
    .resolves({
      data: BOOTSTRAP_SCRIPT,
      statusCode: 200,
      headers: new Headers(),
    });

  return { ...mocks, retrieveBoostrapScriptStub };
};

export const mockIndexPage = (
  sandbox: SinonSandbox,
  { sessions = REBROWSE_SESSIONS_DTOS }: { sessions?: SessionDTO[] } = {}
) => {
  const authMocks = mockAuth(sandbox);
  const countSessionsStub = sandbox
    .stub(SessionApi, 'count')
    .callsFake((args = {}) => {
      return Promise.resolve(
        httpOkResponse(countSessionsBy(sessions, args.search))
      );
    });

  const countPageVisitsStub = sandbox
    .stub(SessionApi.pageVisit, 'count')
    .callsFake((args = {}) => {
      return Promise.resolve(
        httpOkResponse(countSessionsBy(sessions, args.search as any)) as any
      );
    });

  return { ...authMocks, countPageVisitsStub, countSessionsStub };
};
