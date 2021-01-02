import React from 'react';
import { Modal, ModalBody, ModalHeader, SIZE } from 'baseui/modal';
import { PhoneNumberVerifyForm } from 'auth/components/PhoneNumberVerifyForm';
import { toaster } from 'baseui/toast';
import type { UserDTO } from '@rebrowse/types';
import { useIsOpen } from 'shared/hooks/useIsOpen';
import { client } from 'sdk';

type Props = {
  verifyPhoneNumber: (code: number) => Promise<UserDTO>;
  children: (open: () => void) => React.ReactNode;
};

export const PhoneNumberVerifyModal = ({
  verifyPhoneNumber,
  children,
}: Props) => {
  const { isOpen, open, close } = useIsOpen();

  return (
    <>
      {children(open)}
      <Modal
        isOpen={isOpen}
        onClose={close}
        size={SIZE.auto}
        unstable_ModalBackdropScroll
      >
        <ModalHeader>Verify phone number</ModalHeader>
        <ModalBody>
          <PhoneNumberVerifyForm
            sendCode={client.auth.users.phoneNumber.verifySendCode}
            verify={(code) =>
              verifyPhoneNumber(code).then(() => {
                toaster.positive(`Phone number successfully verified`, {});
                close();
              })
            }
          />
        </ModalBody>
      </Modal>
    </>
  );
};
