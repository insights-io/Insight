import React, { useState } from 'react';
import type { PhoneNumber, MfaSetupDTO } from '@rebrowse/types';
import type { HttpResponse } from '@rebrowse/sdk';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { PhoneNumberVerifyForm } from 'signin/components/PhoneNumberVerifyForm';
import { PhoneNumberSetForm } from 'signin/components/PhoneNumberSetForm';

type Props = {
  phoneNumber: PhoneNumber | undefined;
  completeSetup: (code: number) => Promise<HttpResponse<MfaSetupDTO>>;
  onCompleted?: (value: MfaSetupDTO) => void;
};

export const SmsMfaSetupForm = ({
  phoneNumber: initialPhoneNumber,
  completeSetup,
  onCompleted,
}: Props) => {
  const [phoneNumber, setPhoneNumber] = useState(initialPhoneNumber);

  if (!phoneNumber) {
    return (
      <PhoneNumberSetForm
        phoneNumber={phoneNumber}
        updatePhoneNumber={(newPhoneNumber) =>
          client.users.phoneNumber
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
      sendCode={client.mfa.setup.sms.sendCode}
    />
  );
};
