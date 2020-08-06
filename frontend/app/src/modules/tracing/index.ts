/* eslint-disable no-underscore-dangle */
import { IncomingMessage } from 'http';

import { initTracer } from 'jaeger-client';
import {
  Tracer,
  Span,
  Tags,
  FORMAT_HTTP_HEADERS,
  SpanContext,
} from 'opentracing';

let _tracer: Tracer | undefined;

export const getTracer = (): Tracer => {
  if (!_tracer) {
    _tracer = initTracer(
      {
        serviceName: 'frontend-app',
        sampler: { type: 'const', param: 1 },
        reporter: {
          flushIntervalMs: 1000,
          agentHost: process.env.JAEGER_AGENT_HOST as string,
          agentPort: parseInt(process.env.JAEGER_AGENT_PORT as string, 10),
          logSpans: true,
        },
      },
      {}
    );
  }
  return _tracer;
};

export const startRequestSpan = (req: IncomingMessage): Span => {
  const { url, method, headers } = req;
  const tracer = getTracer();
  const wireCtx = tracer.extract(FORMAT_HTTP_HEADERS, headers) || undefined;

  return tracer.startSpan(`${method} ${url}`, {
    childOf: wireCtx,
    tags: {
      [Tags.HTTP_URL]: url,
      [Tags.HTTP_METHOD]: method,
      [Tags.SPAN_KIND]: Tags.SPAN_KIND_RPC_SERVER,
    },
  });
};

export const prepareCrossServiceHeaders = (spanContext: SpanContext | Span) => {
  const headers = {};
  getTracer().inject(spanContext, FORMAT_HTTP_HEADERS, headers);
  return headers;
};
