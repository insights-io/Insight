import type { DataResponse } from './api';

type SessionID = string;
type PageVisitId = string;
type DeviceId = string;

export type PageVisitSessionLink = {
  deviceId: DeviceId;
  sessionId: SessionID;
  pageVisitId: PageVisitId;
};

export type CreatePageResponse = DataResponse<PageVisitSessionLink>;

export type CreatePageVisitDTO = {
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
