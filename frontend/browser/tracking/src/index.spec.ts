/* eslint-disable no-console */
/* eslint-disable no-underscore-dangle */
import path from 'path';
import { createServer, Server } from 'http';
import fs from 'fs';

import { CreatePageResponse } from '@rebrowse/types';
import { chromium, Response, Page } from 'playwright';
import Identity from 'identity';
import type { RebrowseWindow } from 'types';
import type { EventData } from 'event';

declare global {
  // eslint-disable-next-line @typescript-eslint/no-empty-interface
  interface Window extends RebrowseWindow {}
}

const SERVE_PORT = process.env.SERVE_PORT || 5000;
// TODO: should probably use some other organization to not abuse
const I_ORGANIZATION = '000000';
const I_HOST = `localhost:${SERVE_PORT}`;
const beaconApiBaseUrl =
  process.env.BEACON_API_BASE_URL || 'http://localhost:8081';
const sessionApiBaseUrl =
  process.env.SESSION_API_BASE_URL || 'http://localhost:8082';

const parsePageResponse = (response: Response) => {
  return response.body().then<CreatePageResponse>((b) => JSON.parse(String(b)));
};

const responseRequestHeaders = (response: Response) => {
  return response.request().headers() as Record<string, string>;
};

const setupPage = async (page: Page) => {
  await page.goto(`http://${I_HOST}`);
  await page.evaluate(
    ({ organizationId, host }) => {
      window._i_org = organizationId;
      window._i_host = host;
    },
    { organizationId: I_ORGANIZATION, host: I_HOST }
  );

  const trackingScript = path.join(
    process.cwd(),
    'dist',
    'localhost.rebrowse.js'
  );
  await page.addScriptTag({ path: trackingScript });
};

const BROWSERS = [
  { name: 'chromium', instance: chromium },
  // Make it work in CI { name: 'firefox', instance: playwright.firefox },
  // Make it work in CI { name: 'webkit', instance: playwright.webkit },
];

describe('tracking script', () => {
  let server: Server;

  beforeAll(() => {
    jest.setTimeout(60000);
    const pagePath = path.join(process.cwd(), 'templates', 'index.html');
    const pageContents = String(fs.readFileSync(pagePath));
    server = createServer((_req, res) => {
      res.write(pageContents);
      res.end();
    }).listen(SERVE_PORT, () =>
      console.log(`Server running on port ${SERVE_PORT}...`)
    );
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

      console.log('Waiting for session api response...');
      const pageVisitUrl = `${sessionApiBaseUrl}/v1/pages`;
      const pageResponse = await page.waitForResponse(
        (resp: Response) => resp.url() === pageVisitUrl
      );

      const pageRequest = pageResponse.request();
      const pageRequestHeaders = responseRequestHeaders(pageResponse);
      expect(pageResponse.status()).toEqual(200);
      expect(pageRequest.method()).toEqual('POST');
      expect(pageRequestHeaders['content-type']).toEqual('application/json');
      expect(pageRequest.resourceType()).toEqual('fetch');

      const {
        data: { sessionId, deviceId, pageVisitId },
      } = await parsePageResponse(pageResponse);
      const beaconBeatUrl = `${beaconApiBaseUrl}/v1/beacon/beat?organizationId=${I_ORGANIZATION}&sessionId=${sessionId}&deviceId=${deviceId}&pageVisitId=${pageVisitId}`;

      const { cookie, localStorage } = await page.evaluate(() => {
        return {
          cookie: document.cookie,
          localStorage: JSON.stringify(window.localStorage),
        };
      });

      const expiresSeconds = cookie.split('/')[1];
      const encodedIdentity = `${I_HOST}#${I_ORGANIZATION}#${deviceId}:${sessionId}/${expiresSeconds}`;

      expect(cookie).toEqual(`${Identity.storageKey}=${encodedIdentity}`);
      expect(localStorage).toEqual(
        JSON.stringify({ [Identity.storageKey]: encodedIdentity })
      );

      await page.click('button[data-testid="first-button"]');
      await page.click('button[data-testid="xhr-button"]');

      console.log('Waiting for beacon api response...');
      let beaconResponse = await page.waitForResponse(
        (resp: Response) => resp.url() === beaconBeatUrl
      );

      const beaconRequestHeaders = responseRequestHeaders(beaconResponse);

      let beaconRequest = beaconResponse.request();
      let postData = JSON.parse(beaconRequest.postData() || '') as EventData;

      expect(postData.s).toEqual(1);
      expect(beaconResponse.status()).toEqual(204);
      expect(beaconRequest.method()).toEqual('POST');
      expect(beaconRequestHeaders['content-type']).toEqual('application/json');
      expect(beaconRequest.resourceType()).toEqual('fetch');

      const [
        sessionCreateFetchEvent,
        sessionCreatePerformanceResourceEvent,
        mouseMoveEvent,
        xhrButtonClickEvent,
        firstXhrResponseLogEvent,
        firstXhrRequestEvent,
        firstPerformanceResourceEvent,

        secondXhrResponseLogEvent,
        secondXhrRequestEvent,
        secondPerformanceResourceEvent,
      ] = postData.e;

      expect(sessionCreateFetchEvent.a).toEqual([
        'POST',
        pageVisitUrl,
        200,
        'cors',
        'fetch',
        'http/1.1',
      ]);
      expect(sessionCreatePerformanceResourceEvent.a.slice(0, 1)).toEqual([
        pageVisitUrl,
      ]);
      expect(sessionCreatePerformanceResourceEvent.a.slice(3)).toEqual([
        'fetch',
        'http/1.1',
      ]);
      expect(mouseMoveEvent.a).toEqual([
        61,
        60,
        '<BUTTON',
        ':data-testid',
        'first-button',
      ]);
      expect(xhrButtonClickEvent.a).toEqual([
        379,
        60,
        '<BUTTON',
        ':data-testid',
        'xhr-button',
        ':onclick',
        'handleXhrClick()',
      ]);
      expect(firstXhrResponseLogEvent.a).toEqual([
        'log',
        { completed: false, id: 1, title: 'delectus aut autem', userId: 1 },
      ]);

      expect(firstXhrRequestEvent.a).toEqual([
        'GET',
        'https://jsonplaceholder.typicode.com/todos/1',
        200,
        null,
        'xmlhttprequest',
        'h2',
      ]);
      expect(firstPerformanceResourceEvent.a.slice(0, 1)).toEqual([
        'https://jsonplaceholder.typicode.com/todos/1',
      ]);

      expect(firstPerformanceResourceEvent.a.slice(3)).toEqual([
        'xmlhttprequest',
        'h2',
      ]);

      expect({
        ...firstXhrRequestEvent,
        t: secondXhrRequestEvent.t,
      }).toEqual(secondXhrRequestEvent);
      expect({
        ...firstXhrResponseLogEvent,
        t: secondXhrResponseLogEvent.t,
      }).toEqual(secondXhrResponseLogEvent);
      expect(firstPerformanceResourceEvent.a.slice(0, 1)).toEqual(
        secondPerformanceResourceEvent.a.slice(0, 1)
      );
      expect(firstPerformanceResourceEvent.a.slice(3)).toEqual(
        secondPerformanceResourceEvent.a.slice(3)
      );

      await page.evaluate(() => {
        console.info('Do some console.info!');
        console.error('Do some console.error!');
        console.debug({ message: 'Nested' });
        console.warn([{ message: 'Nested' }], 'random');

        // simulate Error thrown in browser (we cant just throw here as this will kill Playwright process)
        const errorElem = document.createElement('script');
        errorElem.textContent = 'throw new Error("simulated error");';
        document.body.append(errorElem);

        // simulate SyntaxError thrown in browser (we cant just throw here as this will kill Playwright process)
        const syntaxErrorElem = document.createElement('script');
        syntaxErrorElem.textContent = 'eval("va x = 5;");';
        document.body.append(syntaxErrorElem);
      });

      console.log('Waiting for beacon api response...');
      beaconResponse = await page.waitForResponse(
        (resp: Response) => resp.url() === beaconBeatUrl
      );
      beaconRequest = beaconResponse.request();
      postData = JSON.parse(beaconRequest.postData() || '') as EventData;

      expect(postData.s).toEqual(2);
      expect(beaconResponse.status()).toEqual(204);

      const [
        beaconBeatFetchEvent,
        beaconBeatPerformanceResourceEvent,
        consoleInfoEvent,
        consoleErrorEvent,
        consoleDebugNestedEvent,
        consoleWarnNestedEvent,
        errorEvent,
        syntaxErrorEvent,
      ] = postData.e;

      expect(beaconBeatFetchEvent.a).toEqual([
        'POST',
        beaconBeatUrl,
        204,
        'cors',
        'fetch',
        'http/1.1',
      ]);
      expect(beaconBeatPerformanceResourceEvent.a.slice(0, 1)).toEqual([
        beaconBeatUrl,
      ]);
      expect(beaconBeatPerformanceResourceEvent.a.slice(3)).toEqual([
        'fetch',
        'http/1.1',
      ]);
      expect(consoleInfoEvent.a).toEqual(['info', 'Do some console.info!']);
      expect(consoleErrorEvent.a).toEqual(['error', 'Do some console.error!']);
      expect(consoleDebugNestedEvent.a).toEqual([
        'debug',
        { message: 'Nested' },
      ]);
      expect(consoleWarnNestedEvent.a).toEqual([
        'warn',
        [{ message: 'Nested' }],
        'random',
      ]);

      expect(errorEvent.a[0]).toEqual('simulated error');
      expect(errorEvent.a[1]).toEqual('Error');
      expect(
        (errorEvent.a[2] as string).includes('Error: simulated error\n    at')
      ).toBeTruthy();

      expect(syntaxErrorEvent.a[0]).toEqual('Unexpected identifier');
      expect(syntaxErrorEvent.a[1]).toEqual('SyntaxError');
      expect(
        (syntaxErrorEvent.a[2] as string).includes(
          'SyntaxError: Unexpected identifier\n    at'
        )
      ).toBeTruthy();

      await browser.close();
    });
  });
});
