import React, { useState } from 'react';
import { AuthApi } from 'api';
import { PhoneNumberSetForm } from 'modules/auth/components/PhoneNumberSetForm';
import { PhoneNumberVerifyForm } from 'modules/auth/components/PhoneNumberVerifyForm';
import type { PhoneNumber, TfaSetupDTO } from '@rebrowse/types';

type Props = {
  phoneNumber: PhoneNumber | undefined;
  completeSetup?: (code: number) => Promise<TfaSetupDTO>;
  onCompleted?: (tfaSetup: TfaSetupDTO) => void;
};

export const SmsMfaSetupForm = ({
  phoneNumber: initialPhoneNumber,
  completeSetup = (code: number) => AuthApi.tfa.setup.complete('sms', code),
  onCompleted,
}: Props) => {
  const [phoneNumber, setPhoneNumber] = useState(initialPhoneNumber);

  if (!phoneNumber) {
    return (
      <PhoneNumberSetForm
        phoneNumber={phoneNumber}
        updatePhoneNumber={(data) =>
          AuthApi.user.updatePhoneNumber(data).then((user) => {
            setPhoneNumber(user.phoneNumber);
            return user;
          })
        }
      />
    );
  }

  return (
    <PhoneNumberVerifyForm
      verify={(code) => completeSetup(code).then(onCompleted)}
      sendCode={AuthApi.tfa.setup.sms.sendCode}
    />
  );
};
