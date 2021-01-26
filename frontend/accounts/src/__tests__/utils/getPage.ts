import {
  BOOTSTRAP_SCRIPT,
  sandbox,
  getPage as originalGetPage,
} from '@rebrowse/testing';
import { client } from 'sdk';

export const getPage: typeof originalGetPage = (options) => {
  sandbox.stub(client.tracking, 'retrieveBoostrapScript').resolves({
    data: BOOTSTRAP_SCRIPT,
    statusCode: 200,
    headers: new Headers(),
  });

  return originalGetPage(options);
};
