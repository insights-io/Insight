import React, { useState } from 'react';
import { PhoneNumberSetForm } from 'auth/components/PhoneNumberSetForm';
import { PhoneNumberVerifyForm } from 'auth/components/PhoneNumberVerifyForm';
import type { PhoneNumber, MfaSetupDTO } from '@rebrowse/types';
import type { HttpResponse } from '@rebrowse/sdk';
import { client, INCLUDE_CREDENTIALS } from 'sdk';

type Props = {
  phoneNumber: PhoneNumber | undefined;
  onCompleted?: (value: MfaSetupDTO) => void;
  completeSetup?: (code: number) => Promise<HttpResponse<MfaSetupDTO>>;
};

const completeSmsSetup = (code: number) =>
  client.auth.mfa.setup.complete('sms', code, INCLUDE_CREDENTIALS);

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
          client.auth.users.phoneNumber
            .update(newPhoneNumber, INCLUDE_CREDENTIALS)
            .then(({ data: user }) => {
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
          .then((httpResponse) => httpResponse.data)
          .then(onCompleted)
      }
      sendCode={client.auth.mfa.setup.sms.sendCode}
    />
  );
};
