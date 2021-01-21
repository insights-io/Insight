export class UnreachableCaseError extends Error {
  public constructor(val: never) {
    super(`Unreachable case: ${val}`);
  }
}
