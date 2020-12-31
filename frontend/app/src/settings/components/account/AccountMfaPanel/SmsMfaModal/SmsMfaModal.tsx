import React, { useCallback } from 'react';
import { Modal, ModalBody, ModalHeader } from 'baseui/modal';
import { toaster } from 'baseui/toast';
import { SmsMfaSetupForm } from 'auth/components/SmsMfaSetupForm';
import { useIsOpen } from 'shared/hooks/useIsOpen';

import { DisableMfaModal } from '../DisableMfaModal';

import type { SmsMfaModalProps } from './types';

export const SMS_LABEL = 'Text message';

export const SmsMfaModal = ({
  isEnabled,
  disable,
  phoneNumber,
  children,
  completeSetup,
}: SmsMfaModalProps) => {
  const { open, close, isOpen } = useIsOpen();

  const onDisable = useCallback(() => {
    disable().then(() => {
      close();
      toaster.positive(`${SMS_LABEL} multi-factor authentication disabled`, {});
    });
  }, [close, disable]);

  const onComplete = useCallback(
    (code: number) => {
      return completeSetup(code).then((httpResponse) => {
        close();
        toaster.positive(
          `${SMS_LABEL} multi-factor authentication enabled`,
          {}
        );
        return httpResponse;
      });
    },
    [close, completeSetup]
  );

  return (
    <>
      {children(open)}
      {isEnabled ? (
        <DisableMfaModal
          header="Are you sure you want to disable text message multi-factor authentication?"
          onConfirm={onDisable}
          isOpen={isOpen}
          close={close}
        />
      ) : (
        <Modal isOpen={isOpen} onClose={close} unstable_ModalBackdropScroll>
          <ModalHeader>
            Configure text message multi-factor authentication
          </ModalHeader>

          <ModalBody>
            <SmsMfaSetupForm
              phoneNumber={phoneNumber}
              completeSetup={onComplete}
            />
          </ModalBody>
        </Modal>
      )}
    </>
  );
};
