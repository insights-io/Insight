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

type Props = RenderPageResult & {
  stylesheets: Sheet[];
  bootstrapScript: string;
};

class InsightDocument extends Document<Props> {
  static async getInitialProps(ctx: DocumentContext): Promise<Props> {
    const page = await ctx.renderPage({
      enhanceApp: (App) => (props) => (
        <StyletronProvider value={styletron}>
          <App {...props} />
        </StyletronProvider>
      ),
    });

    const stylesheets = (styletron as Server).getStylesheets() || [];
    const bootstrapScript = await ky(
      process.env.BOOTSTRAP_SCRIPT ||
        'https://d1l87tz7sw1x04.cloudfront.net/b/development.insight.js'
    ).text();

    return { ...page, stylesheets, bootstrapScript };
  }

  render() {
    return (
      <Html>
        <Head>
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
