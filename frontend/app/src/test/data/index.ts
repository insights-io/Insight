import {
  UserDTO,
  Session,
  SessionDTO,
  BrowserEventDTO,
  User,
} from '@insight/types';
import { v4 as uuid } from 'uuid';
import { subSeconds } from 'date-fns';
import { mapSession, mapUser } from '@insight/sdk';

export const USER_AGENT =
  'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36';

export const INSIGHT_ADMIN_DTO: UserDTO = {
  id: '7c071176-d186-40ac-aaf8-ac9779ab047b',
  email: 'admin@insight.io',
  fullName: 'Admin Admin',
  organizationId: '000000',
  role: 'ADMIN',
  createdAt: new Date().toUTCString(),
};

export const INSIGHT_ADMIN: User = mapUser(INSIGHT_ADMIN_DTO);

export const INSIGHT_SESSION_DTO: SessionDTO = {
  id: uuid(),
  createdAt: new Date().toUTCString(),
  deviceId: '123',
  ipAddress: '127.0.0.1',
  organizationId: INSIGHT_ADMIN.organizationId,
  userAgent: USER_AGENT,
};

export const INSIGHT_SESSION: Session = mapSession(INSIGHT_SESSION_DTO);

export const INSIGHT_SESSION_HOUR_AGO_DTO: SessionDTO = {
  id: uuid(),
  createdAt: subSeconds(new Date(), 3600).toUTCString(),
  deviceId: '123',
  ipAddress: '127.0.0.1',
  organizationId: INSIGHT_ADMIN.organizationId,
  userAgent: USER_AGENT,
};

export const INSIGHT_SESSION_HOUR_AGO: Session = mapSession(
  INSIGHT_SESSION_HOUR_AGO_DTO
);

export const STORYBOK_CONSOLE_WARN_EVENT: BrowserEventDTO = {
  e: 9,
  level: 'warn',
  arguments: [
    `Vendors~main.6e71f8501d51c505cf1d.bundle.js:70044 The default hierarchy separators are changing in Storybook 6.0.
  '|' and '.' will no longer create a hierarchy, but codemods are available.
  Read more about it in the migration guide: https://github.com/storybookjs/storybook/blob/master/MIGRATION.md`,
  ],
  t: 1001,
};

export const FAST_REFRESH_CONSOLE_LOG_EVENT: BrowserEventDTO = {
  e: 9,
  level: 'log',
  arguments: ['[Fast Refresh] done'],
  t: 999,
};
