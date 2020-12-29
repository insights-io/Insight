import React, { useState } from 'react';
import { AuthApi } from 'api';
import { PhoneNumberSetForm } from 'auth/components/PhoneNumberSetForm';
import { PhoneNumberVerifyForm } from 'auth/components/PhoneNumberVerifyForm';
import type { PhoneNumber, MfaSetupDTO, DataResponse } from '@rebrowse/types';
import type { HttpResponse } from '@rebrowse/sdk';

type Props = {
  phoneNumber: PhoneNumber | undefined;
  onCompleted?: (value: MfaSetupDTO) => void;
  completeSetup?: (
    code: number
  ) => Promise<HttpResponse<DataResponse<MfaSetupDTO>>>;
};

const completeSmsSetup = (code: number) =>
  AuthApi.mfa.setup.complete('sms', code);

export const SmsMfaSetupForm = ({
  phoneNumber: initialPhoneNumber,
  completeSetup = completeSmsSetup,
  onCompleted,
}: Props) => {
  const [phoneNumber, setPhoneNumber] = useState(initialPhoneNumber);

  if (!phoneNumber) {
    return (
      <PhoneNumberSetForm
        phoneNumber={phoneNumber}
        updatePhoneNumber={(newPhoneNumber) =>
          AuthApi.user
            .updatePhoneNumber(newPhoneNumber)
            .then(({ data: { data: user } }) => {
              setPhoneNumber(user.phoneNumber);
              return user;
            })
        }
      />
    );
  }

  return (
    <PhoneNumberVerifyForm
      verify={(code) =>
        completeSetup(code)
          .then((httpResponse) => httpResponse.data.data)
          .then(onCompleted)
      }
      sendCode={AuthApi.mfa.setup.sms.sendCode}
    />
  );
};
