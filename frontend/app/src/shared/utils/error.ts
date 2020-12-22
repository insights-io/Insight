import type { ResponsePromise } from 'ky';
import type { APIErrorDataResponse } from '@rebrowse/types';

export class UnreachableCaseError extends Error {
  public constructor(val: never) {
    super(`Unreachable case: ${val}`);
  }
}

export const handleApiError = async (error: unknown) => {
  const typedError = error as { response: ResponsePromise };
  const apiError: APIErrorDataResponse = await typedError.response.json();
  throw apiError;
};
