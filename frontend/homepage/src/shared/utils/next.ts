import { IncomingMessage, ServerResponse } from 'http';

import { NextPageContext } from 'next';

type ServerNextPageContext = NextPageContext & {
  req: IncomingMessage;
  res: ServerResponse;
};

export const isServer = (
  ctx: NextPageContext
): ctx is ServerNextPageContext => {
  return ctx.req !== undefined && ctx.res !== undefined;
};
