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
import { styletron } from 'shared/styles/styletron';
import { Server, Sheet } from 'styletron-engine-atomic';
import { STYLETRON_HYDRATE_CLASSNAME } from '@rebrowse/elements';
import { client } from 'sdk';

type Props = {
  stylesheets: Sheet[];
  bootstrapScript: string;
};

class RebrowseDocument extends Document<Props> {
  static async getInitialProps(ctx: DocumentContext) {
    const page = ctx.renderPage({
      enhanceApp: (App) => (props) => (
        <StyletronProvider value={styletron}>
          <App {...props} />
        </StyletronProvider>
      ),
    });

    const stylesheets = (styletron as Server).getStylesheets() || [];
    const bootstrapScriptUrl = process.env.BOOTSTRAP_SCRIPT as string;
    const bootstrapScript = await client.tracking
      .retrieveBoostrapScript(bootstrapScriptUrl)
      .then((httpResponse) => httpResponse.data);

    return {
      ...page,
      stylesheets,
      bootstrapScript: bootstrapScript.replace('<ORG>', '000000'),
    };
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
          <meta
            name="viewport"
            content="minimum-scale=1, initial-scale=1, width=device-width, shrink-to-fit=no, viewport-fit=cover"
          />
          <meta name="theme-color" content="#000000" />

          <link rel="shortcut icon" href="/assets/favicon.ico" />

          {this.props.stylesheets.map((sheet, i) => (
            <style
              className={STYLETRON_HYDRATE_CLASSNAME}
              dangerouslySetInnerHTML={{ __html: sheet.css }}
              media={sheet.attrs.media}
              data-hydrate={sheet.attrs['data-hydrate']}
              key={i}
            />
          ))}
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
