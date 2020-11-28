import { COUNTRIES, Country } from 'baseui/phone-input';

const COUNTRY_CODE_TO_COUNTRY: Record<string, Country> = Object.values(
  COUNTRIES
).reduce((acc, country: Country) => {
  return {
    ...acc,
    [country.dialCode]: country,
  };
}, {} as Record<string, Country>);

export const getCountryFromCountryCode = (
  countryCode: string | null | undefined,
  defaultCountry: Country = COUNTRIES.US
) => {
  if (!countryCode) {
    return defaultCountry;
  }

  const maybeCountry = COUNTRY_CODE_TO_COUNTRY[countryCode];
  return maybeCountry || defaultCountry;
};
