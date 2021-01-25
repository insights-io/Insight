import { join } from 'path';

import { getPage as originalGetPage } from 'next-page-tester';
import { sandbox } from '@rebrowse/testing';
import { client } from 'sdk';

export const getPage: typeof originalGetPage = (options) => {
  sandbox.stub(client.tracking, 'retrieveBoostrapScript').resolves({
    // TODO: share data?
    data: `((e, s, r) => {
e._i_debug = !1;
e._i_host = 'rebrowse.dev';
e._i_org = '<ORG>';
e._i_ns = 'IS';
const t = s.createElement(r);
t.async = true;
t.crossOrigin = 'anonymous';
t.src = 'https://static.rebrowse.dev/s/rebrowse.js';
const o = s.getElementsByTagName(r)[0];
o.parentNode.insertBefore(t, o);
})(window, document, 'script');`,
    statusCode: 200,
    headers: new Headers(),
  });

  return originalGetPage({
    ...options,
    useDocument: true,
    nonIsolatedModules: ['styletron-react', join(process.cwd(), 'src/sdk')],
  });
};
