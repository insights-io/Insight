import { sandbox } from '@rebrowse/testing';
import { getInitialProps } from 'pages/_document';
import { Server } from 'styletron-engine-atomic';
import * as sdk from '@rebrowse/sdk';
import { mockServerSideRequest } from '@rebrowse/next-testing';
import * as tracerUtils from 'shared/utils/tracing';
import type { Tracer } from 'opentracing';
import { RenderPage, RenderPageResult } from 'next/dist/next-server/lib/utils';

const initilEnvironment = process.env;

describe('pages/_document', () => {
  afterEach(() => {
    process.env = initilEnvironment;
  });

  it('Should render page when pre-rendering', async () => {
    process.env.BOOTSTRAP_SCRIPT = 'fromEnv';
    const renderPage = sandbox
      .stub<Parameters<RenderPage>, RenderPageResult>()
      .resolves({
        html: '<div>Hello world</div>',
      });

    const fetchBoostrapScriptStub = sandbox.stub(sdk, 'getBoostrapScript')
      .resolves(`((s, e, t) => {
      s._i_debug = !1;
      s._i_host = 'rebrowse.dev';
      s._i_org = '<ORG>';
      s._i_ns = 'IS';
      const n = e.createElement(t);
      n.async = true;
      n.crossOrigin = 'anonymous';
      n.src = 'https://static.rebrowse.dev/s/localhost.rebrowse.js';
      const i = e.getElementsByTagName(t)[0];
      i.parentNode.insertBefore(n, i);
    })(window, document, 'script');`);

    const props = await getInitialProps(undefined, renderPage, new Server());

    sandbox.assert.calledWithExactly(fetchBoostrapScriptStub, 'fromEnv');

    expect(props).toEqual({
      html: '<div>Hello world</div>',
      stylesheets: [{ attrs: {}, css: '' }],
      bootstrapScript: `((s, e, t) => {
      s._i_debug = !1;
      s._i_host = 'rebrowse.dev';
      s._i_org = '000000';
      s._i_ns = 'IS';
      const n = e.createElement(t);
      n.async = true;
      n.crossOrigin = 'anonymous';
      n.src = 'https://static.rebrowse.dev/s/localhost.rebrowse.js';
      const i = e.getElementsByTagName(t)[0];
      i.parentNode.insertBefore(n, i);
    })(window, document, 'script');`,
    });
  });

  it('Should trace when request', async () => {
    process.env.BOOTSTRAP_SCRIPT = 'fromEnv';
    const renderPage = sandbox.stub().resolves();
    sandbox.stub(sdk, 'getBoostrapScript').resolves('');

    const tracer = { startSpan: sandbox.stub(), extract: sandbox.stub() };
    sandbox
      .stub(tracerUtils, 'getTracer')
      .returns((tracer as unknown) as Tracer);

    const { req } = mockServerSideRequest();

    const props = await getInitialProps(req, renderPage, new Server());

    sandbox.assert.calledWithExactly(tracer.startSpan, 'GET /', {
      childOf: undefined,
      tags: {
        bootstrapScriptUri: 'fromEnv',
        'http.method': 'GET',
        'http.url': '/',
        'span.kind': 'server',
      },
    });

    expect(props).toEqual({
      stylesheets: [{ attrs: {}, css: '' }],
      bootstrapScript: '',
    });
  });

  it('Should trace continue previously started request trace', async () => {
    process.env.BOOTSTRAP_SCRIPT = 'fromEnv';
    const renderPage = sandbox.stub().resolves();
    sandbox.stub(sdk, 'getBoostrapScript').resolves('');

    const tracer = { startSpan: sandbox.stub(), extract: sandbox.stub() };
    sandbox
      .stub(tracerUtils, 'getTracer')
      .returns((tracer as unknown) as Tracer);

    const { req } = mockServerSideRequest();
    const tracedRequest = req as tracerUtils.IncomingTracedMessage;
    tracedRequest.span = new tracerUtils.Span();

    const props = await getInitialProps(req, renderPage, new Server());

    sandbox.assert.calledWithExactly(
      tracer.startSpan,
      '_document.getInitialProps',
      { childOf: tracedRequest.span, tags: { bootstrapScriptUri: 'fromEnv' } }
    );

    expect(props).toEqual({
      stylesheets: [{ attrs: {}, css: '' }],
      bootstrapScript: '',
    });
  });
});
