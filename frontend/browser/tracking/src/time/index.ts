export const SECONDS_IN_YEAR = 31536e3;

export const MILLIS_IN_SECOND = 1e3;

export const currentTimeSeconds = () => {
  return Math.floor(Date.now() / 1e3);
};

export const yearFromNow = () => {
  return currentTimeSeconds() + SECONDS_IN_YEAR;
};

export const expiresUTC = (expires: number) => {
  return new Date(expires).toUTCString();
};
