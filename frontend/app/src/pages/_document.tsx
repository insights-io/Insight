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
    return `((s, t, e) => {
      s._i_debug = !1;
      s._i_host = 'insight.com';
      s._i_org = '<ORG>';
      s._i_ns = 'IS';
      const n = t.createElement(e);
      n.async = true;
      n.crossOrigin = 'anonymous';
      n.src = 'https://d1l87tz7sw1x04.cloudfront.net/s/development.insight.js';
      const o = t.getElementsByTagName(e)[0];
      o.parentNode.insertBefore(n, o);
    })(window, document, 'script');
    `;
  };

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
            dangerouslySetInnerHTML={{ __html: this.getInsightScript() }}
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
