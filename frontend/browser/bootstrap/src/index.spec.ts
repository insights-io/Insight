/* eslint-disable camelcase */
/* eslint-disable @typescript-eslint/no-non-null-assertion */
/* eslint-disable no-underscore-dangle */
import path from 'path';

import playwright from 'playwright';

declare global {
  interface Window {
    _i_host: string;
    _i_script: string;
    _i_debug: boolean;
    _i_org: string;
    _i_ns: string;
  }
}

describe('bootstrap', () => {
  beforeAll(() => {
    jest.setTimeout(15000);
  });

  it('injects variables and loads tracking script into the page', async () => {
    const browser = await playwright.chromium.launch();
    const context = await browser.newContext();
    const page = await context.newPage();
    const pagePath = path.join(process.cwd(), 'templates', 'index.html');

    await page.goto(`file:${pagePath}`);

    const bundledScriptTag = path.join(process.cwd(), 'dist', 'rebrowse.js');
    await page.addScriptTag({ path: bundledScriptTag });

    const windowHandle = await page.evaluateHandle(() => window);

    const windowResult = await page.evaluate((win) => {
      return {
        host: win._i_host,
        script: win._i_script,
        debug: win._i_debug,
        org: win._i_org,
        namespace: win._i_ns,
      };
    }, windowHandle);

    expect(windowResult).toEqual({
      debug: false,
      host: 'rebrowse.dev',
      namespace: 'IS',
      org: '<ORG>',
    });

    const headHandle = await page.$('head');
    const injectedScriptResult = await page.evaluate((head) => {
      const injectedScript = head!.getElementsByTagName('script')[0];
      return {
        src: injectedScript.src,
        crossOrigin: injectedScript.crossOrigin,
        async: injectedScript.async,
      };
    }, headHandle);

    expect(injectedScriptResult).toEqual({
      src: 'https://static.rebrowse.dev/s/rebrowse.js',
      async: true,
      crossOrigin: 'anonymous',
    });

    await browser.close();
  });
});
