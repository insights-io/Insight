import { sandbox } from '@rebrowse/testing';
import { AuthApi, PagesApi, SessionApi } from 'api';
import {
  REBROWSE_EVENTS,
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_SESSION_INFO,
} from '__tests__/data';
import { jsonPromise, textPromise } from '__tests__/utils';
import ky from 'ky-universal';
import get from 'lodash/get';
import type { BrowserEventDTO, SessionDTO } from '@rebrowse/types';

import {
  filterSession,
  countSessionsBy,
  retrieveSessionMockImplementation,
  searchEventsMockImplementation,
} from './filter';

export const mockAuth = () => {
  const retrieveSessionStub = sandbox
    .stub(AuthApi.sso.session, 'get')
    .returns(jsonPromise({ status: 200, data: REBROWSE_SESSION_INFO }));

  const retrieveUserStub = sandbox
    .stub(AuthApi.user, 'me')
    .resolves(REBROWSE_SESSION_INFO.user);

  const retrieveOrganizationStub = sandbox
    .stub(AuthApi.organization, 'get')
    .resolves(REBROWSE_SESSION_INFO.organization);

  return { retrieveSessionStub, retrieveUserStub, retrieveOrganizationStub };
};

export const mockSessionDetailsPage = (
  sessions: SessionDTO[] = REBROWSE_SESSIONS_DTOS,
  events: BrowserEventDTO[] = REBROWSE_EVENTS
) => {
  const authMocks = mockAuth();

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
  sessions: SessionDTO[] = REBROWSE_SESSIONS_DTOS
) => {
  const authMocks = mockAuth();

  const countSessionsStub = sandbox
    .stub(SessionApi, 'count')
    .callsFake((args = {}) =>
      Promise.resolve(countSessionsBy(sessions, args.search))
    );

  const retrieveSessionStub = sandbox
    .stub(SessionApi, 'getSession')
    .callsFake((id) => retrieveSessionMockImplementation(id, sessions));

  const listSessionsStub = sandbox
    .stub(SessionApi, 'getSessions')
    .callsFake((args = {}) =>
      Promise.resolve(sessions.filter((s) => filterSession(s, args.search)))
    );

  const getDistinctStub = sandbox
    .stub(SessionApi, 'distinct')
    .callsFake((on: string) =>
      Promise.resolve([
        ...new Set(sessions.map((s) => get(s, on)).filter(Boolean)),
      ])
    );

  return {
    ...authMocks,
    listSessionsStub,
    countSessionsStub,
    getDistinctStub,
    retrieveSessionStub,
  };
};

export const mockEmptySessionsPage = () => {
  const authMocks = mockAuth();
  const countSessionsStub = sandbox.stub(SessionApi, 'count').resolves([]);
  const listSessionsStub = sandbox.stub(SessionApi, 'getSessions').resolves([]);

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
    ...authMocks,
    listSessionsStub,
    countSessionsStub,
    retrieveRecordingSnippetStub,
  };
};

export const mockIndexPage = (
  sessions: SessionDTO[] = REBROWSE_SESSIONS_DTOS
) => {
  const authMocks = mockAuth();
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
