import { DataResponse } from './api';

type SessionID = string;
type PageID = string;
type DeviceId = string;

export type PageIdentity = {
  deviceId: DeviceId;
  sessionId: SessionID;
  pageId: PageID;
};

export type CreatePageResponse = DataResponse<PageIdentity>;

export type CreatePageDTO = {
  organizationId: string;
  deviceId?: string;
  url: string;
  width: number;
  height: number;
  screenWidth: number;
  screenHeight: number;
  referrer: string;
  doctype: string;
  compiledTs: number;
};
