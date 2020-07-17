// eslint-disable-next-line @typescript-eslint/ban-types
export type GlobalObject = Window | (NodeJS.Global & typeof globalThis) | {};

class Context {
  private readonly startTime: number;

  constructor() {
    this.startTime = this.getTime();
  }

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
