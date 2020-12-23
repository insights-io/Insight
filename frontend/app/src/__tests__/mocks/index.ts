import { AuthApi, PagesApi, SessionApi } from 'api';
import {
  REBROWSE_EVENTS,
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_SESSION_INFO,
} from '__tests__/data';
import { jsonPromise, textPromise } from '__tests__/utils/request';
import ky from 'ky-universal';
import type {
  BrowserEventDTO,
  SessionDTO,
  SessionInfoDTO,
} from '@rebrowse/types';
import { SinonSandbox } from 'sinon';

import {
  filterSession,
  countSessionsBy,
  retrieveSessionMockImplementation,
  searchEventsMockImplementation,
} from './filter';
import { getParsedValue } from './filter/core';

export const mockAuth = (
  sandbox: SinonSandbox,
  data: SessionInfoDTO = REBROWSE_SESSION_INFO
) => {
  const retrieveSessionStub = sandbox
    .stub(AuthApi.sso.session, 'get')
    .returns(jsonPromise({ status: 200, data }));

  const retrieveUserStub = sandbox.stub(AuthApi.user, 'me').resolves(data.user);

  const retrieveOrganizationStub = sandbox
    .stub(AuthApi.organization, 'get')
    .resolves(data.organization);

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
      Promise.resolve(countSessionsBy(sessions, args.search))
    );

  const listSessionsStub = sandbox
    .stub(SessionApi, 'getSessions')
    .callsFake((args = {}) =>
      Promise.resolve(sessions.filter((s) => filterSession(s, args.search)))
    );

  const getDistinctStub = sandbox
    .stub(SessionApi, 'distinct')
    .callsFake((on) => {
      return Promise.resolve([
        ...new Set(
          sessions
            .map((session) => getParsedValue(session, on) as string)
            .filter(Boolean)
        ),
      ]);
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

  const retrieveRecordingSnippetStub = sandbox.stub(ky, 'get').returns(
    textPromise({
      status: 200,
      data: `((e, s, r) => {
    e._i_debug = !1;
    e._i_host = 'rebrowse.dev';
    e._i_org = '<ORG>';
    e._i_ns = 'IS';
    const t = s.createElement(r);
    t.async = true;
    t.crossOrigin = 'anonymous';
    t.src = 'https://static.rebrowse.dev/s/rebrowse.js';
    const o = s.getElementsByTagName(r)[0];
    o.parentNode.insertBefore(t, o);
  })(window, document, 'script');`,
    })
  );

  return {
    ...mocks,
    retrieveRecordingSnippetStub,
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
      Promise.resolve(countSessionsBy(sessions, args.search))
    );

  const countPageVisitsStub = sandbox
    .stub(PagesApi, 'count')
    .callsFake((args = {}) =>
      Promise.resolve(countSessionsBy(sessions, args.search))
    );

  return { ...authMocks, countPageVisitsStub, countSessionsStub };
};
