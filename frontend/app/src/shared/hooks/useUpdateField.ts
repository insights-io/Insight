import { toaster } from 'baseui/toast';
import { useState } from 'react';

type UseUpdateFieldOptions<
  U,
  K extends keyof R,
  R extends Record<string, unknown>
> = {
  currentValue: R[K];
  fieldName: K;
  resource: string;
  update: (update: U) => Promise<R>;
};

const buildUpdateTextMessage = <
  K extends keyof R,
  R extends Record<string, unknown>,
  C extends unknown
>(
  resource: string,
  fieldName: K,
  updatedResource: R,
  currentValue: C
) => {
  const nextValue = updatedResource[fieldName];
  if (nextValue === true) {
    return `Successfully enabled ${resource} ${fieldName}`;
  }
  if (nextValue === false) {
    return `Successfully disabled ${resource} ${fieldName}`;
  }

  let message = `Successfully cleared ${resource} ${fieldName}`;
  if (nextValue) {
    message = currentValue
      ? `Successfully changed ${resource} ${fieldName} from "${currentValue}" to "${nextValue}"`
      : `Successfully changed ${resource} ${fieldName} to "${nextValue}"`;
  }

  return message;
};

export const useUpdateField = <
  K extends keyof R,
  R extends Record<string, unknown>,
  U
>({
  fieldName,
  resource,
  currentValue,
  update,
}: UseUpdateFieldOptions<U, K, R>) => {
  const [updating, setUpdating] = useState(false);
  const [value, setValue] = useState<R[K]>(currentValue);

  const updateValueImpl = (nextValue: R[K]) => {
    if (nextValue === currentValue) {
      return Promise.resolve();
    }

    setUpdating(true);
    return update(({ [fieldName]: nextValue } as unknown) as U)
      .then((updatedResource) => {
        toaster.positive(
          buildUpdateTextMessage(
            resource,
            fieldName,
            updatedResource,
            currentValue
          ),
          {}
        );
      })
      .catch(() => {
        setValue(currentValue);
        toaster.negative(
          `Something went wrong while trying to update ${resource}`,
          {}
        );
      })
      .finally(() => setUpdating(false));
  };

  const updateNext = (nextValue: R[K]) => {
    setValue(nextValue);
    return updateValueImpl(nextValue).catch(() => setValue(value));
  };

  const updateCurrent = () => {
    updateValueImpl(value);
  };

  return {
    value,
    setValue,
    updating,
    updateCurrent,
    updateNext,
  };
};
