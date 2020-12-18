import { APIError } from '@rebrowse/types';

export const mockApiError = (apiError: APIError): Error => {
  const error = new Error('APIError');

  Object.assign(error, {
    response: {
      json: () => ({ error: apiError }),
      status: apiError.statusCode,
    },
  });

  return error;
};
