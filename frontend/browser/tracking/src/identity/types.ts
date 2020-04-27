import { PageIdentity } from 'backend/types';

export type InsightIdentity = {
  orgId: string;
  uid: string;
  sessionId: string;
  host: string;
  expiresSeconds: number;
};

export type Cookie = Partial<InsightIdentity>;

export interface Connected {
  connect(identity: PageIdentity): void;
}
