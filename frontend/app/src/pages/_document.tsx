/* eslint-disable react/no-array-index-key */
/* eslint-disable react/no-danger */
import { IncomingMessage } from 'http';

import React from 'react';
import Document, {
  Html,
  Head,
  Main,
  NextScript,
  DocumentContext,
} from 'next/document';
import { Provider as StyletronProvider } from 'styletron-react';
import { styletron } from 'shared/styles/styletron';
import { Server, Sheet } from 'styletron-engine-atomic';
import type { RenderPageResult } from 'next/dist/next-server/lib/utils';
import {
  startSpan,
  IncomingTracedMessage,
  startRequestSpan,
  Span,
} from 'shared/utils/tracing';
import { getBoostrapScript } from '@rebrowse/sdk';
import { STYLETRON_HYDRATE_CLASSNAME } from '@rebrowse/elements';

type Props = RenderPageResult & {
  stylesheets: Sheet[];
  bootstrapScript: string;
};

export const getInitialProps = async (
  req: IncomingMessage | undefined,
  renderPage: DocumentContext['renderPage'],
  serverStyletron: Server
): Promise<Props> => {
  const bootstrapScriptUri = process.env.BOOTSTRAP_SCRIPT as string;
  let span: Span | undefined;

  // Request will be undefined if we are prerendering page
  if (req) {
    const incomingTracedMessage = req as IncomingTracedMessage;
    const tags = { bootstrapScriptUri };
    span = incomingTracedMessage.span
      ? startSpan('_document.getInitialProps', {
          childOf: incomingTracedMessage.span,
          tags,
        })
      : startRequestSpan(incomingTracedMessage, tags);
  }

  const bootstrapScriptPromise = getBoostrapScript(bootstrapScriptUri);
  const renderPagePromise = renderPage({
    enhanceApp: (App) => (props) => (
      <StyletronProvider value={styletron}>
        <App {...props} />
      </StyletronProvider>
    ),
  });

  const stylesheets = serverStyletron.getStylesheets() || [];

  try {
    const [page, bootstrapScript] = await Promise.all([
      renderPagePromise,
      bootstrapScriptPromise,
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
};

class RebrowseDocument extends Document<Props> {
  static async getInitialProps(ctx: DocumentContext): Promise<Props> {
    return getInitialProps(ctx.req, ctx.renderPage, styletron as Server);
  }

  render() {
    return (
      <Html lang="en">
        <Head>
          <meta name="application-name" content="Rebrowse" />
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

          <link rel="shortcut icon" href="/assets/favicon.ico" />
          <link rel="manifest" href="/assets/manifest.json" />

          <style>
            {`
              html, body, #__next {
                height: 100%;
                margin: 0px;
              }
              *, *::before, *::after {
                box-sizing: border-box;
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

export default RebrowseDocument;
