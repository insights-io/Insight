import React from 'react';
import { Modal, ModalBody, ModalHeader, SIZE } from 'baseui/modal';
import { PhoneNumberVerifyForm } from 'modules/auth/components/PhoneNumberVerifyForm';
import { AuthApi } from 'api';
import { toaster } from 'baseui/toast';
import type { UserDTO } from '@rebrowse/types';
import { useIsOpen } from 'shared/hooks/useIsOpen';

type Props = {
  setUser: (user: UserDTO) => void;
  children: (open: () => void) => React.ReactNode;
};

export const PhoneNumberVerifyModal = ({ setUser, children }: Props) => {
  const { isOpen, open, close } = useIsOpen();

  return (
    <>
      {children(open)}
      <Modal isOpen={isOpen} onClose={close} size={SIZE.auto}>
        <ModalHeader>Verify phone number</ModalHeader>
        <ModalBody>
          <PhoneNumberVerifyForm
            sendCode={AuthApi.user.phoneNumberVerifySendCode}
            verify={(code) =>
              AuthApi.user.phoneNumberVerify(code).then((user) => {
                toaster.positive(`Phone number successfully verified`, {});
                setUser(user);
                close();
              })
            }
          />
        </ModalBody>
      </Modal>
    </>
  );
};