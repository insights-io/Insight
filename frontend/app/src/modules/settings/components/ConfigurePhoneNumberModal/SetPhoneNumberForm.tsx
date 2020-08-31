import { UpdateUserPayload } from '@insight/sdk/dist/auth';
import { User } from '@insight/types';
import { PhoneNumberInput } from '@insight/ui';
import { Button, SHAPE, SIZE } from 'baseui/button';
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';

import { getCountryFromPhoneNumber } from './utils';

type Data = {
  phoneNumber: string | null;
};

type Props = {
  phoneNumber: string | null;
  onPhoneNumberSet: () => void;
  updateUser: (user: UpdateUserPayload) => Promise<User>;
};

const SetPhoneNumberForm = ({
  phoneNumber,
  updateUser,
  onPhoneNumberSet,
}: Props) => {
  const [country, setCountry] = useState(() =>
    getCountryFromPhoneNumber(phoneNumber)
  );

  const [isSubmitting, setIsSubmitting] = useState(false);
  const { handleSubmit, errors, control } = useForm<Data>({
    defaultValues: {
      phoneNumber: phoneNumber?.split(country.dialCode)[1] || undefined,
    },
  });

  const onSubmit = handleSubmit(async (data) => {
    if (isSubmitting) {
      return;
    }

    const nextPhoneNumber = `${country.dialCode}${data.phoneNumber}`;
    if (nextPhoneNumber === phoneNumber) {
      onPhoneNumberSet();
      return;
    }

    setIsSubmitting(true);
    try {
      await updateUser({ phone_number: nextPhoneNumber });
      onPhoneNumberSet();
    } finally {
      setIsSubmitting(false);
    }
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
    </form>
  );
};

export default React.memo(SetPhoneNumberForm);
