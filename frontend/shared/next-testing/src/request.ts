import type { IncomingMessage, ServerResponse } from 'http';

import { sandbox } from '@rebrowse/testing';

export const mockServerSideRequest = () => {
  const writeHead = sandbox.stub();
  const end = sandbox.stub();
  const res = ({ writeHead, end } as unknown) as ServerResponse;
  const req = {
    url: '/',
    method: 'GET',
  } as IncomingMessage;

  return { req, res, writeHead, end };
};
