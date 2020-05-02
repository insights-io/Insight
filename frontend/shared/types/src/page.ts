import { DataResponse } from './api';

type SessionID = string;
type PageID = string;
type UID = string;

export type PageIdentity = {
  uid: UID;
  sessionId: SessionID;
  pageId: PageID;
};

export type CreatePageResponse = DataResponse<PageIdentity>;
