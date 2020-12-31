import React from 'react';
import { Modal, ModalBody, ModalHeader } from 'baseui/modal';
import { TotpMfaSetupForm } from 'auth/components/TotpMfaSetupForm';

import type { Props } from './types';

export const TotpMfaSetupModal = ({ isOpen, onClose, ...rest }: Props) => {
  return (
    <Modal onClose={onClose} isOpen={isOpen} unstable_ModalBackdropScroll>
      <ModalHeader>Setup multi-factor authentication</ModalHeader>
      <ModalBody>
        <TotpMfaSetupForm {...rest} />
      </ModalBody>
    </Modal>
  );
};
