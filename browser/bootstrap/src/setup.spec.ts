/* eslint-disable @typescript-eslint/no-non-null-assertion */
/* eslint-disable jest/no-standalone-expect */
/* eslint-disable no-underscore-dangle */
import playwright from 'playwright';
import path from 'path';
import assert from 'assert';

declare global {
  interface Window {
    _i_host: string;
    _i_script: string;
    _i_debug: boolean;
    _i_org: string;
    _i_ns: string;
  }
}

// test that correct values are injected into the window object and script is loaded
(async () => {
  const browser = await playwright.chromium.launch();
  const context = await browser.newContext();

  const pagePath = path.join(process.cwd(), 'templates', 'index.html');
  const page = await context.newPage(`file:${pagePath}`);

  const bundledScriptTag = path.join(process.cwd(), 'dist', 'index.js');
  await page.addScriptTag({ path: bundledScriptTag });

  const windowHandle = await page.evaluateHandle(() => window);

  const windowResult = await page.evaluate(win => {
    return {
      host: win._i_host,
      script: win._i_script,
      debug: win._i_debug,
      org: win._i_org,
      namespace: win._i_ns,
    };
  }, windowHandle);

  assert(windowResult.host === 'insight.com');
  assert(windowResult.script === 'cdn.insight.com/s/is.js');
  assert(windowResult.debug === false);
  assert(windowResult.org === '<ORG>');
  assert(windowResult.namespace === 'IS');

  const headHandle = await page.$('head');
  const injectedScriptResult = await page.evaluate(head => {
    const injectedScript = head!.getElementsByTagName('script')[0];
    return {
      src: injectedScript.src,
      crossOrigin: injectedScript.crossOrigin,
      async: injectedScript.async,
    };
  }, headHandle);

  assert(injectedScriptResult.src === 'https://cdn.insight.com/s/is.js');
  assert(injectedScriptResult.async === true);
  assert(injectedScriptResult.crossOrigin === 'anonymous');

  await browser.close();
})();
