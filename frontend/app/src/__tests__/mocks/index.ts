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
  SessionDTO,
  SessionInfoDTO,
} from '@rebrowse/types';
import type { SinonSandbox } from 'sinon';
import { jsonPromise } from '__tests__/utils';
import { BOOTSTRAP_SCRIPT } from '__tests__/data/recording';

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

  const retrieveUserStub = sandbox.stub(AuthApi.user, 'me').resolves({
    data: { data: data.user },
    statusCode: 200,
    headers: new Headers(),
  });

  const retrieveOrganizationStub = sandbox
    .stub(AuthApi.organization, 'get')
    .resolves({
      data: { data: data.organization },
      statusCode: 200,
      headers: new Headers(),
    });

  return { retrieveSessionStub, retrieveUserStub, retrieveOrganizationStub };
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
    .callsFake((args = {}) =>
      Promise.resolve({
        data: { data: countSessionsBy(sessions, args.search) },
        statusCode: 200,
        headers: new Headers(),
      })
    );

  const listSessionsStub = sandbox
    .stub(SessionApi, 'getSessions')
    .callsFake((args = {}) =>
      Promise.resolve({
        data: { data: sessions.filter((s) => filterSession(s, args.search)) },
        statusCode: 200,
        headers: new Headers(),
      })
    );

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

  return {
    ...mocks,
    retrieveBoostrapScriptStub,
  };
};

export const mockIndexPage = (
  sandbox: SinonSandbox,
  { sessions = REBROWSE_SESSIONS_DTOS }: { sessions?: SessionDTO[] } = {}
) => {
  const authMocks = mockAuth(sandbox);
  const countSessionsStub = sandbox
    .stub(SessionApi, 'count')
    .callsFake((args = {}) =>
      Promise.resolve({
        data: { data: countSessionsBy(sessions, args.search) },
        statusCode: 200,
        headers: new Headers(),
      })
    );

  const countPageVisitsStub = sandbox
    .stub(SessionApi.pageVisit, 'count')
    .callsFake((args = {}) => {
      return Promise.resolve({
        data: {
          data: countSessionsBy(sessions, args.search as any),
        },
        statusCode: 200,
        headers: new Headers(),
      }) as any;
    });

  return { ...authMocks, countPageVisitsStub, countSessionsStub };
};
