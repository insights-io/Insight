import React, { useState } from 'react';
import type { Meta } from '@storybook/react';
import { COUNTRIES, Country } from 'baseui/phone-input';
import { useForm } from 'react-hook-form';

import { PhoneNumberInput, Values } from './PhoneNumberInput';

export default {
  title: 'inputs/PhoneNumberInput',
  component: PhoneNumberInput,
} as Meta;

const usePhoneNumberInput = () => {
  const { control } = useForm<Values>();
  const [country, setCountry] = useState<Country>(COUNTRIES.AF);

  return { control, country, setCountry };
};

export const Base = () => {
  return <PhoneNumberInput {...usePhoneNumberInput()} error={undefined} />;
};
