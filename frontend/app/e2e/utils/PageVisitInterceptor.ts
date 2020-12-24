import { RequestHook } from 'testcafe';

export class PageVisitInterceptor extends RequestHook {
  private sessionId: string | undefined;

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

  public static create = () => {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    return new PageVisitInterceptor(/\/v1\/pages/, { includeBody: true });
  };
}
