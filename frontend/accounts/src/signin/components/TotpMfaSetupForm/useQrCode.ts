import type { APIError, APIErrorDataResponse } from '@rebrowse/types';
import { useEffect, useState } from 'react';
import { client, INCLUDE_CREDENTIALS } from 'sdk';

export const useQrImage = () => {
  const [qrImage, setQrImage] = useState<string>();
  const [qrImageError, setQrImageError] = useState<APIError>();

  useEffect(() => {
    client.mfa.setup.totp
      .start(INCLUDE_CREDENTIALS)
      .then((dataResponse) => setQrImage(dataResponse.data.qrImage))
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setQrImageError(errorDTO.error);
      });
  }, [setQrImage]);

  return { qrImageError, qrImage };
};
