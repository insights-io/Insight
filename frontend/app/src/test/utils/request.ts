import type { ResponsePromise } from 'ky';

type ResponsePromiseParams<D> = {
  status: number;
  data?: D;
};

export const responsePromise = <D>({
  status,
  data,
}: ResponsePromiseParams<D>): ResponsePromise => {
  const promise = Promise.resolve({ status }) as ResponsePromise;
  promise.json = <T>() => Promise.resolve(({ data } as unknown) as T);
  return promise;
};
