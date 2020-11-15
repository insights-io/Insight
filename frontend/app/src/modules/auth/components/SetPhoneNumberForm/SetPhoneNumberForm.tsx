import React, { useState } from 'react';
import { Button, PhoneNumberInput } from '@insight/elements';
import { useForm } from 'react-hook-form';
import FormError from 'shared/components/FormError';
import type {
  APIError,
  APIErrorDataResponse,
  PhoneNumber,
  UserDTO,
} from '@insight/types';

import { getCountryFromPhoneNumber } from './utils';

type Data = {
  phoneNumber: string | undefined;
};

type Props = {
  initialValue: PhoneNumber | null;
  updatePhoneNumber: (phoneNumber: PhoneNumber | null) => Promise<UserDTO>;
  onContinue?: () => void;
};

export const SetPhoneNumberForm = ({
  initialValue,
  updatePhoneNumber,
  onContinue,
}: Props) => {
  const [formError, setFormError] = useState<APIError | undefined>();
  const [country, setCountry] = useState(() =>
    getCountryFromPhoneNumber(initialValue)
  );

  const [isSubmitting, setIsSubmitting] = useState(false);
  const { handleSubmit, errors, control } = useForm<Data>({
    defaultValues: {
      phoneNumber: initialValue?.digits || undefined,
    },
  });

  const onSubmit = handleSubmit(async (data) => {
    if (isSubmitting) {
      return;
    }

    const nextPhoneNumber = `${country.dialCode}${data.phoneNumber}`;
    if (
      nextPhoneNumber === `${initialValue?.countryCode}${initialValue?.digits}`
    ) {
      onContinue?.();
      return;
    }

    setIsSubmitting(true);
    updatePhoneNumber(
      data.phoneNumber
        ? { countryCode: country.dialCode, digits: data.phoneNumber }
        : null
    )
      .then(onContinue)
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <form noValidate onSubmit={onSubmit}>
      <PhoneNumberInput
        country={country}
        setCountry={setCountry}
        error={errors?.phoneNumber}
        control={control}
      />
      <Button isLoading={isSubmitting} type="submit" $style={{ width: '100%' }}>
        Continue
      </Button>
      {formError && <FormError error={formError} />}
    </form>
  );
};
