import type { PageVisitSessionLink } from '@rebrowse/types';

export type RebrowseIdentity = {
  organizationId: string;
  deviceId: string;
  sessionId: string;
  host: string;
  expiresSeconds: number;
};

export type Cookie = Partial<RebrowseIdentity>;

export interface Connected {
  connect(identity: PageVisitSessionLink): void;
}
