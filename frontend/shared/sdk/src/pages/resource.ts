import ky from 'ky-universal';
import type { DataResponse } from '@rebrowse/types';

import { getData, querystring, withCredentials } from '../utils';

import type { PageVisitSearchRequestOptions } from './types';

export const createPagesClient = (sessionApiBaseUrl: string) => {
  function count<T = { count: number }>({
    baseURL = sessionApiBaseUrl,
    search,
    ...rest
  }: PageVisitSearchRequestOptions = {}) {
    const searchQuery = querystring(search);
    return ky
      .get(`${baseURL}/v1/pages/count${searchQuery}`, withCredentials(rest))
      .json<DataResponse<T>>()
      .then(getData);
  }

  const PagesApi = { count };

  return PagesApi;
};
