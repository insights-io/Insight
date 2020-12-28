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

export type PageVisitDimensions = {
  width: number;
  height: number;
  screenWidth: number;
  screenHeight: number;
};

export type PageVisitCreateParams = PageVisitDimensions & {
  organizationId: string;
  deviceId: string | undefined;
  referrer: string;
  doctype: string;
  compiledTs: number;
  href: string;
};

export type PageVisitDTO = PageVisitDimensions & {
  organizationId: string;
  referrer: string;
  doctype: string;
  compiledTs: number;
  origin: string;
  path: string;
};
