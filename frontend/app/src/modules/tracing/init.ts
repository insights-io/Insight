/* eslint-disable global-require */
/* eslint-disable @typescript-eslint/no-var-requires */
/* eslint-disable class-methods-use-this */
/* eslint-disable no-underscore-dangle */
import { TracingConfig } from 'jaeger-client';
import { MockTracer as OpentracingMockTracer, Tracer } from 'opentracing';

class FixedMockTracer extends OpentracingMockTracer {
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-ignore
  protected _inject(_span: unknown, _format: unknown, _carrier: unknown): void {
    return undefined;
  }

  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-ignore
  // eslint-disable-next-line lodash/prefer-constant
  protected _extract(_format: unknown, _carrier: unknown) {
    return null;
  }
}

export const initTracer = (config: TracingConfig): Tracer => {
  try {
    const jeager = require('jaeger-client');
    return jeager.initTracer(config, {}) as Tracer;
  } catch (e) {
    // eslint-disable-next-line no-console
    console.log(
      `Failed to initialize jeager-client: ${e.message}. Initializing mock tracer.`
    );
    return new FixedMockTracer();
  }
};
