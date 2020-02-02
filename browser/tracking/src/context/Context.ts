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
}

export default Context;
