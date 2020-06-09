import { ClientFunction } from 'testcafe';

export * from './sso';
export * from './mail';

export const getLocation = ClientFunction(() => document.location.href);
