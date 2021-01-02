import { sandbox } from '@rebrowse/testing';
import { getInitialProps } from 'pages/_document';
import { Server } from 'styletron-engine-atomic';
import { mockServerSideRequest } from '@rebrowse/next-testing';
import * as tracerUtils from 'shared/utils/tracing';
import type { Tracer } from 'opentracing';
import { RenderPage, RenderPageResult } from 'next/dist/next-server/lib/utils';
import { BOOTSTRAP_SCRIPT } from '__tests__/data/recording';
import { client } from 'sdk';

const initilEnvironment = process.env;

describe('pages/_document', () => {
  afterEach(() => {
    process.env = initilEnvironment;
  });

  it('Should render page when pre-rendering', async () => {
    process.env.BOOTSTRAP_SCRIPT = 'fromEnv';
    const renderPage = sandbox
      .stub<Parameters<RenderPage>, RenderPageResult>()
      .resolves({ html: '<div>Hello world</div>' });

    const fetchBoostrapScriptStub = sandbox
      .stub(client.tracking, 'retrieveBoostrapScript')
      .resolves({
        data: BOOTSTRAP_SCRIPT,
        statusCode: 200,
        headers: new Headers(),
      });

    const props = await getInitialProps(undefined, renderPage, new Server());

    sandbox.assert.calledWithExactly(fetchBoostrapScriptStub, 'fromEnv');

    expect(props).toEqual({
      html: '<div>Hello world</div>',
      stylesheets: [{ attrs: {}, css: '' }],
      bootstrapScript: BOOTSTRAP_SCRIPT,
    });
  });

  it('Should trace when request', async () => {
    process.env.BOOTSTRAP_SCRIPT = 'fromEnv';
    const renderPage = sandbox.stub().resolves();
    sandbox
      .stub(client.tracking, 'retrieveBoostrapScript')
      .resolves({ data: '', headers: new Headers(), statusCode: 200 });

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
    sandbox
      .stub(client.tracking, 'retrieveBoostrapScript')
      .resolves({ data: '', statusCode: 200, headers: new Headers() });

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
