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

type Props = {
  stylesheets: Sheet[];
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
    return { ...page, stylesheets };
  }

  getInsightScript = () => {
    return {
      __html: `((s, t, e) => {
      s._i_debug = !1;
      s._i_host = 'insight.com';
      s._i_org = 'try123';
      s._i_ns = 'IS';
      const n = t.createElement(e);
      n.async = true;
      n.crossOrigin = 'anonymous';
      n.src = 'https://d1l87tz7sw1x04.cloudfront.net/s/development.insight.js';
      const o = t.getElementsByTagName(e)[0];
      o.parentNode.insertBefore(n, o);
    })(window, document, 'script');
    `,
    };
  };

  render() {
    return (
      <Html lang="en">
        <Head>
          <meta name="viewport" content="width=device-width, initial-scale=1" />
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
          <script dangerouslySetInnerHTML={this.getInsightScript()} />
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
