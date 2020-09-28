import { UpdateUserPayload } from '@insight/sdk/dist/auth';
import { PhoneNumberInput } from '@insight/elements';
import { Button, SHAPE, SIZE } from 'baseui/button';
import React, { useState } from 'react';
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
  phoneNumber: string | null;
};

type Props = {
  phoneNumber: PhoneNumber | null;
  onPhoneNumberSet: () => void;
  updateUser: (user: UpdateUserPayload) => Promise<UserDTO>;
};

const SetPhoneNumberForm = ({
  phoneNumber,
  updateUser,
  onPhoneNumberSet,
}: Props) => {
  const [formError, setFormError] = useState<APIError | undefined>();
  const [country, setCountry] = useState(() =>
    getCountryFromPhoneNumber(phoneNumber)
  );

  const [isSubmitting, setIsSubmitting] = useState(false);
  const { handleSubmit, errors, control } = useForm<Data>({
    defaultValues: {
      phoneNumber: phoneNumber?.digits || undefined,
    },
  });

  const onSubmit = handleSubmit(async (data) => {
    if (isSubmitting) {
      return;
    }

    const nextPhoneNumber = `${country.dialCode}${data.phoneNumber}`;
    if (
      nextPhoneNumber === `${phoneNumber?.countryCode}${phoneNumber?.digits}`
    ) {
      onPhoneNumberSet();
      return;
    }

    setIsSubmitting(true);
    updateUser({
      phone_number: data.phoneNumber
        ? { countryCode: country.dialCode, digits: data.phoneNumber }
        : null,
    })
      .then(() => onPhoneNumberSet())
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
      <Button
        isLoading={isSubmitting}
        type="submit"
        $style={{ width: '100%' }}
        shape={SHAPE.pill}
        size={SIZE.compact}
      >
        Next
      </Button>
      {formError && <FormError error={formError} />}
    </form>
  );
};

export default React.memo(SetPhoneNumberForm);
