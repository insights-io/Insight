import type { DataResponse } from '@rebrowse/types';

import type { HttpResponse, ResponsePromise } from '../types';

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

const json = <TObject>(responsePromise: ResponsePromise) => {
  return Promise.all([responsePromise, responsePromise.json<TObject>()]);
};

export const jsonDataResponse = async <TObject>(
  responsePromise: ResponsePromise
): Promise<HttpResponse<TObject>> => {
  const [response, data] = await json<DataResponse<TObject>>(responsePromise);
  return httpDataResponse<TObject>(data.data, response);
};
