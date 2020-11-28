import React, { useState } from 'react';
import type { PhoneNumber, TfaSetupDTO } from '@rebrowse/types';
import { AuthApi } from 'api';
import { PhoneNumberSetForm } from 'modules/auth/components/PhoneNumberSetForm';
import { PhoneNumberVerifyForm } from 'modules/auth/components/PhoneNumberVerifyForm';

type Props = {
  phoneNumber: PhoneNumber | undefined;
  completeSetup?: typeof AuthApi.tfa.setup.complete;
  onCompleted?: (tfaSetup: TfaSetupDTO) => void;
};

export const SmsMfaSetupForm = ({
  phoneNumber: initialPhoneNumber,
  completeSetup = AuthApi.tfa.setup.complete,
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
      verify={(code) => completeSetup('sms', code).then(onCompleted)}
      sendCode={AuthApi.tfa.setup.sms.sendCode}
    />
  );
};
