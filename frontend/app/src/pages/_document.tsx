/* eslint-disable react/no-array-index-key */
/* eslint-disable react/no-danger */
import React from 'react';
import Document, {
  Html,
  Head,
  Main,
  NextScript,
  DocumentContext,
} from 'next/document';
import { Provider as StyletronProvider } from 'styletron-react';
import {
  styletron,
  STYLETRON_HYDRATE_CLASSNAME,
} from 'shared/styles/styletron';
import { Server, Sheet } from 'styletron-engine-atomic';
import ky from 'ky-universal';
import { RenderPageResult } from 'next/dist/next-server/lib/utils';
import {
  startSpan,
  IncomingTracedMessage,
  startRequestSpan,
  Span,
} from 'modules/tracing';

type Props = RenderPageResult & {
  stylesheets: Sheet[];
  bootstrapScript: string;
};

class InsightDocument extends Document<Props> {
  static async getInitialProps(ctx: DocumentContext): Promise<Props> {
    const bootstrapScriptURI = process.env.BOOTSTRAP_SCRIPT as string;
    const tags = { bootstrapScriptURI };

    let span: Span | undefined;

    // Request will be undefined if we are prerendering page
    if (ctx.req) {
      const incomingTracedMessage = ctx.req as IncomingTracedMessage;
      span = incomingTracedMessage.span
        ? startSpan('_document.getInitialProps', {
            childOf: incomingTracedMessage.span,
            tags,
          })
        : startRequestSpan(incomingTracedMessage, tags);
    }

    const fetchBootstrapScriptPromise = ky(bootstrapScriptURI).text();
    const renderPagePromise = ctx.renderPage({
      enhanceApp: (App) => (props) => (
        <StyletronProvider value={styletron}>
          <App {...props} />
        </StyletronProvider>
      ),
    });

    const stylesheets = (styletron as Server).getStylesheets() || [];

    try {
      const [page, bootstrapScript] = await Promise.all([
        renderPagePromise,
        fetchBootstrapScriptPromise,
      ]);

      return {
        ...page,
        stylesheets,
        bootstrapScript: bootstrapScript.replace('<ORG>', '000000'),
      };
    } finally {
      if (span) {
        span.finish();
      }
    }
  }

  render() {
    return (
      <Html lang="en">
        <Head>
          <meta name="application-name" content="Insight" />
          <meta name="apple-mobile-web-app-capable" content="yes" />
          <meta
            name="apple-mobile-web-app-status-bar-style"
            content="default"
          />
          <meta
            name="Description"
            content="Find insights into your frontend applications."
          />
          <meta name="mobile-web-app-capable" content="yes" />
          <meta name="theme-color" content="#000000" />
          <meta
            name="viewport"
            content="minimum-scale=1, initial-scale=1, width=device-width, shrink-to-fit=no, viewport-fit=cover"
          />

          <link rel="shortcut icon" href="/static/favicon.ico" />
          <link rel="manifest" href="/static/manifest.json" />

          <style>
            {`
              html, body, #__next {
                height: 100%;
                margin: 0px;
              }
            `}
          </style>

          {this.props.stylesheets.map((sheet, i) => (
            <style
              className={STYLETRON_HYDRATE_CLASSNAME}
              dangerouslySetInnerHTML={{ __html: sheet.css }}
              media={sheet.attrs.media}
              data-hydrate={sheet.attrs['data-hydrate']}
              key={i}
            />
          ))}

          <script
            dangerouslySetInnerHTML={{ __html: this.props.bootstrapScript }}
          />
        </Head>

        <body>
          <Main />
          <NextScript />
        </body>
      </Html>
    );
  }
}

export default InsightDocument;
