import { useState, useMemo, useRef } from 'react';
import type { APIError, APIErrorDataResponse } from '@rebrowse/types';

type Props<T> = {
  submitAction: (code: number) => Promise<T>;
  handleError: (
    error: APIErrorDataResponse,
    setError: React.Dispatch<React.SetStateAction<APIError | undefined>>
  ) => void;
  length?: number;
};

export const useCodeInput = <T>({
  submitAction,
  handleError,
  length = 6,
}: Props<T>) => {
  const [code, setCode] = useState(() => Array(length).fill(''));
  const [apiError, setApiError] = useState<APIError | undefined>();
  const submitButtonRef = useRef<HTMLButtonElement>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const codeError = useMemo(() => {
    return apiError?.errors?.code;
  }, [apiError]);

  const handleSubmit = (values: string[]) => {
    if (values.includes('')) {
      setApiError({
        message: 'Bad Request',
        reason: 'Bad Request',
        statusCode: 400,
        errors: { code: 'Required' },
      });
      return;
    }

    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setApiError(undefined);
    submitAction(Number(values.join('')))
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        handleError(errorDTO, setApiError);
      })
      .finally(() => setIsSubmitting(false));
  };

  const handleChange = (values: string[]) => {
    setCode(values);
    setApiError(undefined);
    if (!values.includes('')) {
      submitButtonRef.current?.focus();
    }
  };

  return {
    handleChange,
    code,
    submitButtonRef,
    handleSubmit,
    codeError,
    isSubmitting,
    apiError,
  };
};
