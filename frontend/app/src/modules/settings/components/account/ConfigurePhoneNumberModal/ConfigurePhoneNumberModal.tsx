import React, { useCallback, useState } from 'react';
import { Modal, SIZE } from 'baseui/modal';
import { ProgressSteps, Step } from 'baseui/progress-steps';
import { PhoneNumber, UserDTO } from '@rebrowse/types';
import { SetPhoneNumberForm } from 'modules/auth/components/SetPhoneNumberForm';

import VerifyPhoneNumberForm from './VerifyPhoneNumberForm';

type Props = {
  isOpen: boolean;
  setIsModalOpen: (isOpen: boolean) => void;
  phoneNumber: PhoneNumber | null;
  updatePhoneNumber: (phoneNumber: PhoneNumber | null) => Promise<UserDTO>;
  setUser: (user: UserDTO) => void;
};

export const ConfigurePhoneNumberModal = ({
  isOpen,
  setIsModalOpen,
  phoneNumber,
  updatePhoneNumber,
  setUser,
}: Props) => {
  const [currentStep, setCurrentStep] = useState(0);

  const onContinue = useCallback(() => {
    setCurrentStep((s) => s + 1);
  }, []);

  const onBack = useCallback(() => {
    setCurrentStep((s) => s - 1);
  }, []);

  const onPhoneNumberVerified = useCallback(
    (user: UserDTO) => {
      setUser(user);
      setIsModalOpen(false);
    },
    [setIsModalOpen, setUser]
  );

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => setIsModalOpen(false)}
      size={SIZE.auto}
    >
      <ProgressSteps current={currentStep}>
        <Step title="Set phone number">
          <SetPhoneNumberForm
            initialValue={phoneNumber}
            onContinue={onContinue}
            updatePhoneNumber={updatePhoneNumber}
          />
        </Step>

        <Step title="Verify phone number">
          <VerifyPhoneNumberForm
            onBack={onBack}
            onPhoneNumberVerified={onPhoneNumberVerified}
          />
        </Step>
      </ProgressSteps>
    </Modal>
  );
};
