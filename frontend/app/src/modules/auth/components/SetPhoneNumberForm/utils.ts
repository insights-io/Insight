import { PhoneNumber } from '@insight/types';
import { COUNTRIES, Country } from 'baseui/phone-input';

export const getCountryFromPhoneNumber = (
  phoneNumber: PhoneNumber | null,
  defaultCountry: Country = COUNTRIES.US
) => {
  if (!phoneNumber) {
    return defaultCountry;
  }

  const maybeCountry: Country | undefined = Object.values(COUNTRIES).find(
    (c) => {
      return phoneNumber.countryCode === (c as Country).dialCode;
    }
  );

  return maybeCountry || defaultCountry;
};
