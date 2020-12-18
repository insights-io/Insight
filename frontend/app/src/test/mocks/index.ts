import { sandbox } from '@rebrowse/testing';
import { AuthApi, PagesApi, SessionApi } from 'api';
import {
  REBROWSE_EVENTS,
  REBROWSE_SESSIONS_DTOS,
  REBROWSE_SESSION_INFO,
} from 'test/data';
import { jsonPromise, textPromise } from 'test/utils';
import ky from 'ky-universal';
import get from 'lodash/get';
import type { SessionDTO } from '@rebrowse/types';
import { mockApiError } from '@rebrowse/storybook';

import { filterBrowserEvent, filterSession } from './filter';

export const mockAuth = () => {
  const retrieveSessionStub = sandbox.stub(AuthApi.sso.session, 'get').returns(
    jsonPromise({
      status: 200,
      data: REBROWSE_SESSION_INFO,
    })
  );

  const retrieveUserStub = sandbox
    .stub(AuthApi.user, 'me')
    .resolves(REBROWSE_SESSION_INFO.user);

  const retrieveOrganizationStub = sandbox
    .stub(AuthApi.organization, 'get')
    .resolves(REBROWSE_SESSION_INFO.organization);

  return { retrieveSessionStub, retrieveUserStub, retrieveOrganizationStub };
};

export const mockSessionDetailsPage = (
  sessions: SessionDTO[] = REBROWSE_SESSIONS_DTOS
) => {
  const authMocks = mockAuth();

  const retrieveSessionStub = sandbox
    .stub(SessionApi, 'getSession')
    .callsFake((id) => {
      const maybeSession = sessions.find((s) => s.id === id);
      if (maybeSession) {
        return Promise.resolve(maybeSession);
      }
      return Promise.reject(
        mockApiError({
          statusCode: 404,
          message: 'Not Found',
          reason: 'Not Found',
        })
      );
    });

  const searchEventsStub = sandbox
    .stub(SessionApi.events, 'search')
    .callsFake((_sessionId, args = {}) => {
      return Promise.resolve(
        REBROWSE_EVENTS.filter((e) => filterBrowserEvent(e, args.search))
      );
    });

  return { ...authMocks, retrieveSessionStub, searchEventsStub };
};

export const mockSessionsPage = (
  sessions: SessionDTO[] = REBROWSE_SESSIONS_DTOS
) => {
  const authMocks = mockAuth();

  const countSessionsStub = sandbox
    .stub(SessionApi, 'count')
    .callsFake((args = {}) => {
      return Promise.resolve({
        count: sessions.filter((s) => filterSession(s, args.search)).length,
      });
    });

  const retrieveSessionStub = sandbox
    .stub(SessionApi, 'getSession')
    .callsFake((id) => {
      const maybeSession = sessions.find((s) => s.id === id);
      if (maybeSession) {
        return Promise.resolve(maybeSession);
      }
      return Promise.reject(
        mockApiError({
          statusCode: 404,
          message: 'Not Found',
          reason: 'Not Found',
        })
      );
    });

  const listSessionsStub = sandbox
    .stub(SessionApi, 'getSessions')
    .callsFake((args = {}) => {
      return Promise.resolve(
        sessions.filter((s) => filterSession(s, args.search))
      );
    });

  const getDistinctStub = sandbox
    .stub(SessionApi, 'distinct')
    .callsFake((on: string) => {
      return Promise.resolve([
        ...new Set(sessions.map((s) => get(s, on)).filter(Boolean)),
      ]);
    });

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

export const mockIndexPage = () => {
  const authMocks = mockAuth();
  const countPagesStub = sandbox.stub(PagesApi, 'count').resolves([]);
  const countSessionsStub = sandbox.stub(SessionApi, 'count').resolves([]);

  return { ...authMocks, countPagesStub, countSessionsStub };
};
