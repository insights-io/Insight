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
import ky from 'ky-universal';
import { Server, Sheet } from 'styletron-engine-atomic';

type Props = {
  stylesheets: Sheet[];
  bootstrapScript: string;
};

class InsightDocument extends Document<Props> {
  static async getInitialProps(ctx: DocumentContext) {
    const page = ctx.renderPage({
      enhanceApp: (App) => (props) => (
        <StyletronProvider value={styletron}>
          <App {...props} />
        </StyletronProvider>
      ),
    });

    const stylesheets = (styletron as Server).getStylesheets() || [];
    const bootstrapScriptURI = process.env.BOOTSTRAP_SCRIPT as string;
    const bootstrapScript = await ky(bootstrapScriptURI).text();

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
          <meta name="viewport" content="width=device-width, initial-scale=1" />
          <meta
            name="Description"
            content="Find insights into your frontend applications."
          />
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
              [data-baseweb="phone-input"] {
                background-color: white !important;
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

export default InsightDocument;
