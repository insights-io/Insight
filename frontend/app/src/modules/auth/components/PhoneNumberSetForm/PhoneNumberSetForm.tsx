import React, { useState } from 'react';
import { Button, Label, PhoneNumberInput } from '@rebrowse/elements';
import { Controller, FieldError, useForm } from 'react-hook-form';
import { FormError } from 'shared/components/FormError';
import type {
  APIError,
  APIErrorDataResponse,
  PhoneNumber,
  UserDTO,
} from '@rebrowse/types';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';

type Data = {
  phoneNumber: PhoneNumber | undefined;
};

type Props = {
  phoneNumber: PhoneNumber | undefined;
  updatePhoneNumber: (phoneNumber: PhoneNumber | undefined) => Promise<UserDTO>;
  onContinue?: () => void;
};

export const PhoneNumberSetForm = ({
  phoneNumber,
  updatePhoneNumber,
  onContinue,
}: Props) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();

  const { handleSubmit, errors, control } = useForm<Data>({
    defaultValues: { phoneNumber },
  });

  const onSubmit = handleSubmit(async (data) => {
    if (isSubmitting) {
      return;
    }

    if (JSON.stringify(data) === JSON.stringify(phoneNumber)) {
      onContinue?.();
      return;
    }

    setIsSubmitting(true);
    updatePhoneNumber(data.phoneNumber)
      .then(onContinue)
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <form noValidate onSubmit={onSubmit}>
      <FormControl
        label={<Label>Phone number</Label>}
        error={(errors.phoneNumber as FieldError)?.message}
      >
        <Controller
          name="phoneNumber"
          control={control}
          as={
            <PhoneNumberInput
              error={Boolean((errors.phoneNumber as FieldError)?.message)}
              placeholder="51111222"
            />
          }
        />
      </FormControl>

      <Block marginTop="16px">
        <Button
          isLoading={isSubmitting}
          type="submit"
          $style={{ width: '100%' }}
        >
          Continue
        </Button>
      </Block>
      {formError && <FormError error={formError} />}
    </form>
  );
};
