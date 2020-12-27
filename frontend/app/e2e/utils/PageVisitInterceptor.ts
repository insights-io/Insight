import { RequestHook } from 'testcafe';

/* Used to intercept "sessionId" from tracking script page visit request.
 * Has to be re-created for each fixture
 */
export class PageVisitInterceptor extends RequestHook {
  private sessionId: string | undefined;

  constructor() {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    super(/\/v1\/pages/, { includeBody: true });
  }

  // eslint-disable-next-line class-methods-use-this
  async onRequest() {
    return Promise.resolve(null);
  }

  async onResponse(response: Response) {
    const dataResponse = JSON.parse(String(response.body));
    this.sessionId = dataResponse.data.sessionId;
  }

  getSessionId() {
    if (!this.sessionId) {
      throw new Error(
        'Session ID not defined. Please ensure all relevant services are running'
      );
    }

    return this.sessionId;
  }
}
