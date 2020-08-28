import { Button } from 'baseui/button';
import { Modal, ModalBody, ModalFooter, ModalHeader } from 'baseui/modal';
import { COUNTRIES, Country } from 'baseui/phone-input';
import React, { useState } from 'react';
import { PhoneNumberInput } from '@insight/ui';
import { useForm } from 'react-hook-form';

type Props = {
  isOpen: boolean;
  setIsModalOpen: (isOpen: boolean) => void;
  phoneNumber: string | null;
};

type ConfigurePhoneNumberDTO = {
  phoneNumber: string;
};

const getCountryFromPhoneNumber = (
  phoneNumber: string | null,
  defaultCountry: Country = COUNTRIES.US
) => {
  if (!phoneNumber) {
    return defaultCountry;
  }

  const maybeCountry: Country | undefined = Object.values(COUNTRIES).find(
    (c) => {
      return phoneNumber.startsWith((c as Country).dialCode);
    }
  );

  return maybeCountry || defaultCountry;
};

const ConfigurePhoneNumberModal = ({
  isOpen,
  setIsModalOpen,
  phoneNumber,
}: Props) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [country, setCountry] = useState(() =>
    getCountryFromPhoneNumber(phoneNumber)
  );

  const { handleSubmit, errors, control } = useForm<ConfigurePhoneNumberDTO>({
    defaultValues: {
      phoneNumber: phoneNumber?.split(country.dialCode)[1] || undefined,
    },
  });

  const onSubmit = handleSubmit((_data) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);
  });

  return (
    <Modal isOpen={isOpen} onClose={() => setIsModalOpen(false)}>
      <ModalHeader>Configure phone number</ModalHeader>
      <form noValidate onSubmit={onSubmit}>
        <ModalBody>
          <PhoneNumberInput<ConfigurePhoneNumberDTO>
            country={country}
            setCountry={setCountry}
            error={errors?.phoneNumber}
            control={control}
          />
        </ModalBody>
        <ModalFooter>
          <Button isLoading={isSubmitting} type="submit">
            Submit
          </Button>
        </ModalFooter>
      </form>
    </Modal>
  );
};

export default React.memo(ConfigurePhoneNumberModal);
