import { useState, useMemo, useRef } from 'react';
import { APIError, APIErrorDataResponse } from '@insight/types';

type Props<T> = {
  submitAction: (code: number) => Promise<T>;
  handleError: (
    error: APIErrorDataResponse,
    setError: React.Dispatch<React.SetStateAction<APIError | undefined>>
  ) => void;
};

const useTfaInput = <T>({ submitAction, handleError }: Props<T>) => {
  const [code, setCode] = useState(() => Array(6).fill(''));
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
        errors: {
          code: 'Required',
        },
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
    if (!values.includes('')) {
      submitButtonRef.current?.focus();
    } else {
      setApiError(undefined);
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

export default useTfaInput;
