// eslint-disable-next-line @typescript-eslint/ban-types
export type GlobalObject = Window | (NodeJS.Global & typeof globalThis) | {};

class Context {
  private readonly startTime: number;
  private seq: number;

  constructor() {
    this.startTime = this.getTime();
    this.seq = 0;
  }

  public incrementAndGetSeq = (): number => {
    this.seq += 1;
    return this.seq;
  };

  private getTime = (): number => {
    return new Date().getTime();
  };

  public now = (): number => {
    return this.getTime() - this.startTime;
  };

  public static getGlobalObject = (): GlobalObject => {
    if (typeof window !== 'undefined') {
      return window;
    }

    if (typeof global !== 'undefined') {
      return global;
    }

    // eslint-disable-next-line no-restricted-globals
    if (typeof self !== 'undefined') {
      // eslint-disable-next-line no-restricted-globals
      return self;
    }

    return {};
  };
}

export default Context;
