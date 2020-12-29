import type { ResponsePromise } from 'ky';
import type { HttpResponse } from 'types';

export const httpResponse = ({ status: statusCode, headers }: Response) => {
  return { statusCode, headers };
};

export const httpDataResponse = <TObject>(
  data: TObject,
  response: Response
): HttpResponse<TObject> => {
  return { data, ...httpResponse(response) };
};

export const textResponse = async (
  responsePromise: ResponsePromise
): Promise<HttpResponse<string>> => {
  const response = await responsePromise;
  const data = await responsePromise.text();
  return httpDataResponse(data, response);
};

export const jsonResponse = async <TObject>(
  responsePromise: ResponsePromise
): Promise<HttpResponse<TObject>> => {
  const response = await responsePromise;
  const data = await responsePromise.json<TObject>();
  return httpDataResponse(data, response);
};
