/* eslint-disable @typescript-eslint/no-explicit-any */
import { AuthApi, SessionApi } from 'api';
import {
  REBROWSE_EVENTS,
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_SESSION_INFO,
} from '__tests__/data';
import * as sdk from '@rebrowse/sdk';
import type {
  BrowserEventDTO,
  SamlConfigurationDTO,
  SamlSsoMethod,
  SessionDTO,
  SessionInfoDTO,
  SsoSetupDTO,
} from '@rebrowse/types';
import type { SinonSandbox } from 'sinon';
import { httpOkResponse, jsonPromise } from '__tests__/utils/request';
import { BOOTSTRAP_SCRIPT } from '__tests__/data/recording';
import { mockApiError } from '@rebrowse/storybook';

import {
  filterSession,
  countSessionsBy,
  retrieveSessionMockImplementation,
  searchEventsMockImplementation,
  getDistinctMockImplementation,
} from './filter';

export const mockAuth = (
  sandbox: SinonSandbox,
  data: SessionInfoDTO = REBROWSE_SESSION_INFO
) => {
  const retrieveSessionStub = sandbox
    .stub(AuthApi.sso.session, 'get')
    .returns(jsonPromise({ status: 200, data }));

  const retrieveUserStub = sandbox
    .stub(AuthApi.user, 'me')
    .resolves(httpOkResponse(data.user));

  const retrieveOrganizationStub = sandbox
    .stub(AuthApi.organization, 'get')
    .resolves(httpOkResponse(data.organization));

  return { retrieveSessionStub, retrieveUserStub, retrieveOrganizationStub };
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
      searchEventsMockImplementation(args.search, events)
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
        httpOkResponse(sessions.filter((s) => filterSession(s, args.search)))
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
