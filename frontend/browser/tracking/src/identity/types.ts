import { PageIdentity } from '@insight/types';

export type InsightIdentity = {
  organizationId: string;
  deviceId: string;
  sessionId: string;
  host: string;
  expiresSeconds: number;
};

export type Cookie = Partial<InsightIdentity>;

export interface Connected {
  connect(identity: PageIdentity): void;
}
