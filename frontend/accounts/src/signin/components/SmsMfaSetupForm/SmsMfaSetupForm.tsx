import React, { useState } from 'react';
import type { PhoneNumber } from '@rebrowse/types';
import type { AuthorizationSuccessResponse, HttpResponse } from '@rebrowse/sdk';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { PhoneNumberVerifyForm } from 'signin/components/PhoneNumberVerifyForm';
import { PhoneNumberSetForm } from 'signin/components/PhoneNumberSetForm';

type Props = {
  phoneNumber: PhoneNumber | undefined;
  completeSetup: (
    code: number
  ) => Promise<HttpResponse<AuthorizationSuccessResponse>>;
  onCompleted?: (value: AuthorizationSuccessResponse) => void;
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
      sendCode={client.accounts.sendSmsCode}
      verify={(code) =>
        completeSetup(code)
          .then((httpResponse) => httpResponse.data)
          .then(onCompleted)
      }
    />
  );
};
