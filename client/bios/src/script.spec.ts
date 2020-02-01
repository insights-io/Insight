/* eslint-disable @typescript-eslint/no-non-null-assertion */
/* eslint-disable jest/no-standalone-expect */
/* eslint-disable no-underscore-dangle */
import playwright from 'playwright';
import path from 'path';
import assert from 'assert';

declare global {
  interface Window {
    _is_host: string;
    _is_script: string;
    _is_debug: boolean;
    _is_org: string;
    _is_namespace: string;
  }
}

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
      host: win._is_host,
      script: win._is_script,
      debug: win._is_debug,
      org: win._is_org,
      namespace: win._is_namespace,
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
