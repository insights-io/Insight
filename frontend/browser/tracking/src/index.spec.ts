/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-console */
/* eslint-disable @typescript-eslint/camelcase */
/* eslint-disable no-underscore-dangle */
import path from 'path';
import { createServer, Server } from 'http';
import fs from 'fs';

import playwright from 'playwright';

const SERVE_PORT = 5000;
const I_ORG = 'test-1';
const I_HOST = `localhost:${SERVE_PORT}`;

// TODO: resuse shared types
type PageResponse = {
  data: { uid: string; sessionId: string; pageId: string };
};

const parsePageResponse = (response: playwright.Response) => {
  return response.body().then<PageResponse>((b) => JSON.parse(String(b)));
};

const setupPage = async (page: playwright.Page) => {
  await page.goto(`http://${I_HOST}`);
  await page.evaluate(
    ({ orgID, host }) => {
      window._i_org = orgID;
      window._i_host = host;
    },
    { orgID: I_ORG, host: I_HOST }
  );

  const insightScript = path.join(process.cwd(), 'dist', 'local.insight.js');
  await page.addScriptTag({ path: insightScript });
};

const BROWSERS = [
  { name: 'chromium', instance: playwright.chromium },
  // Make it work in CI { name: 'firefox', instance: playwright.firefox },
  // Make it work in CI { name: 'webkit', instance: playwright.webkit },
];

describe('tracking script', () => {
  let server: Server;

  beforeAll(() => {
    jest.setTimeout(30000);
    const pagePath = path.join(process.cwd(), 'templates', 'index.html');
    const pageContents = String(fs.readFileSync(pagePath));
    server = createServer((_req, res) => {
      res.write(pageContents);
      res.end();
    }).listen(SERVE_PORT, () => console.log('Server up'));
  });

  afterAll(() => {
    server.close();
  });

  BROWSERS.forEach(({ name, instance }) => {
    test(`[${name}]: persists identity in cookie & local storage`, async () => {
      const browser = await instance.launch();
      const context = await browser.newContext();
      const page = await context.newPage();

      await setupPage(page);

      const response = await page.waitForResponse(
        async (resp: playwright.Response) => {
          const request = resp.request();
          const headers = request.headers() as Record<string, string>;
          return (
            resp.url() === 'http://localhost:8082/v1/sessions' &&
            resp.status() === 200 &&
            request.method() === 'POST' &&
            headers['content-type'] === 'application/json' &&
            request.resourceType() === 'fetch'
          );
        }
      );

      const {
        data: { sessionId, uid },
      } = await parsePageResponse(response);

      const { cookie, localStorage } = await page.evaluate(() => {
        return {
          cookie: document.cookie,
          localStorage: JSON.stringify(window.localStorage),
        };
      });

      const expiresSeconds = cookie.split('/')[1];
      const encodedIdentity = `${I_HOST}#${I_ORG}#${uid}:${sessionId}/${expiresSeconds}`;

      const storageKey = '_is_uid';
      expect(cookie).toEqual(`${storageKey}=${encodedIdentity}`);
      expect(localStorage).toEqual(
        JSON.stringify({ [storageKey]: encodedIdentity })
      );

      await browser.close();
    });
  });
});
