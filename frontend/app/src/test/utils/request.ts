import type { ResponsePromise } from 'ky';

type ResponsePromiseParams<D> = {
  status: number;
  data?: D;
};

export const jsonPromise = <D>({
  status,
  data,
}: ResponsePromiseParams<D>): ResponsePromise => {
  const promise = Promise.resolve({ status }) as ResponsePromise;
  promise.json = <T>() => Promise.resolve(({ data } as unknown) as T);
  return promise;
};

export const textPromise = ({
  status,
  data,
}: ResponsePromiseParams<string>): ResponsePromise => {
  const promise = Promise.resolve({ status }) as ResponsePromise;
  promise.text = () => Promise.resolve(data as string);
  return promise;
};
